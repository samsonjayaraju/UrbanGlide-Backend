package com.urbanglide.rideservice;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.urbanglide.rideservice.client.*;
import com.urbanglide.rideservice.dto.*;
import com.urbanglide.rideservice.enums.*;
import com.urbanglide.rideservice.exception.InvalidRideStatusException;
import com.urbanglide.rideservice.repository.*;
import com.urbanglide.rideservice.service.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(RideIntegrationTests.TestClients.class)
class RideIntegrationTests {
  // Small handwritten in-memory clients replace only the two remote services.
  // Controllers, JWT filters, business services, transactions and JPA remain real.
  @TestConfiguration
  static class TestClients {
    @Bean
    @Primary
    FakeDriverClient driverClient() {
      return new FakeDriverClient();
    }

    @Bean
    @Primary
    FakePaymentClient paymentClient() {
      return new FakePaymentClient();
    }
  }

  static class FakeDriverClient implements DriverClient {
    final Map<Long, Long> reservations = new ConcurrentHashMap<>();
    boolean empty;

    public List<NearbyDriverResponse> nearby(double lat, double lon, double radius) {
      return empty
          ? List.of()
          : List.of(new NearbyDriverResponse(10L, 0.1), new NearbyDriverResponse(20L, 0.2));
    }

    public DriverIdentity byUser(Long id) {
      return new DriverIdentity(id, id);
    }

    public synchronized void reserve(Long id, ReservationRequest request) {
      Long active = reservations.get(id);
      if (active != null && !active.equals(request.rideId()))
        throw new com.urbanglide.rideservice.exception.RideAlreadyAssignedException("Busy");
      reservations.put(id, request.rideId());
    }

    public void release(Long id, ReservationRequest request) {
      reservations.remove(id, request.rideId());
    }
  }

  static class FakePaymentClient implements PaymentClient {
    final Map<Long, CreatePaymentRequest> records = new ConcurrentHashMap<>();
    boolean fail;

    public void create(CreatePaymentRequest request) {
      if (fail)
        throw new com.urbanglide.rideservice.exception.ApiException(
            org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE, "Payment offline");
      records.putIfAbsent(request.rideId(), request);
    }
  }

  @Autowired MockMvc mvc;
  @Autowired ObjectMapper json;
  @Autowired RideRepository rides;
  @Autowired RideOfferRepository offers;
  @Autowired FakeDriverClient drivers;
  @Autowired FakePaymentClient payments;
  @Autowired FareService fares;

  @BeforeEach
  void clean() {
    offers.deleteAll();
    rides.deleteAll();
    drivers.reservations.clear();
    drivers.empty = false;
    payments.records.clear();
    payments.fail = false;
  }

  private String passenger() {
    return TestTokens.bearer(1, "PASSENGER");
  }

  private String driver(long id) {
    return TestTokens.bearer(id, "DRIVER");
  }

  private long create() throws Exception {
    String response =
        mvc.perform(
                post("/api/rides")
                    .header("Authorization", passenger())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
{"pickupLocation":"Location A","pickupLatitude":14.4426,"pickupLongitude":79.9865,
 "dropLocation":"Location B","dropLatitude":14.4700,"dropLongitude":79.9900}
"""))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
    return json.readTree(response).get("rideId").asLong();
  }

  private int accept(long ride, long driver) throws Exception {
    return mvc.perform(
            post("/api/rides/" + ride + "/accept").header("Authorization", driver(driver)))
        .andReturn()
        .getResponse()
        .getStatus();
  }

  private void advance(long ride, String next, int expected) throws Exception {
    mvc.perform(
            patch("/api/rides/" + ride + "/status")
                .header("Authorization", driver(10))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"" + next + "\"}"))
        .andExpect(status().is(expected));
  }

  @Test
  void completeRideLifecycleAndRetryPaymentFailure() throws Exception {
    long id = create();
    assertEquals(RideStatus.SEARCHING, rides.findById(id).orElseThrow().getStatus());
    assertEquals(2, offers.count());
    mvc.perform(get("/api/rides/driver/offers").header("Authorization", driver(10)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].ride.rideId").value(id));
    assertEquals(200, accept(id, 10));
    assertEquals(409, accept(id, 20));
    assertEquals(
        RideOfferStatus.CANCELLED,
        offers.findByRideIdAndDriverId(id, 20L).orElseThrow().getStatus());
    advance(id, "IN_PROGRESS", 400);
    mvc.perform(
            patch("/api/rides/" + id + "/status")
                .header("Authorization", driver(20))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"ARRIVING\"}"))
        .andExpect(status().isForbidden());
    advance(id, "ARRIVING", 200);
    advance(id, "ARRIVED", 200);
    advance(id, "IN_PROGRESS", 200);
    payments.fail = true;
    advance(id, "COMPLETED", 503);
    assertEquals(RideStatus.COMPLETED, rides.findById(id).orElseThrow().getStatus());
    assertFalse(rides.findById(id).orElseThrow().getCompletionSynced());
    payments.fail = false;
    advance(id, "COMPLETED", 200);
    advance(id, "COMPLETED", 200);
    assertEquals(1, payments.records.size());
    assertTrue(drivers.reservations.isEmpty());
    assertTrue(rides.findById(id).orElseThrow().getCompletionSynced());
    advance(id, "ARRIVING", 400);
    mvc.perform(get("/api/rides/my").header("Authorization", passenger()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].status").value("COMPLETED"));
    mvc.perform(get("/api/rides/" + id).header("Authorization", TestTokens.bearer(2, "PASSENGER")))
        .andExpect(status().isForbidden());
  }

  @Test
  void concurrentAcceptHasExactlyOneWinner() throws Exception {
    long id = create();
    CountDownLatch start = new CountDownLatch(1);
    try (ExecutorService pool = Executors.newFixedThreadPool(2)) {
      Future<Integer> first =
          pool.submit(
              () -> {
                start.await();
                return accept(id, 10);
              });
      Future<Integer> second =
          pool.submit(
              () -> {
                start.await();
                return accept(id, 20);
              });
      start.countDown();
      var results =
          new ArrayList<>(
              List.of(first.get(15, TimeUnit.SECONDS), second.get(15, TimeUnit.SECONDS)));
      Collections.sort(results);
      assertEquals(List.of(200, 409), results);
    }
    assertEquals(1, drivers.reservations.size());
    assertEquals(
        1,
        offers.findByRideId(id).stream()
            .filter(o -> o.getStatus() == RideOfferStatus.ACCEPTED)
            .count());
  }

  @Test
  void oneDriverCannotAcceptTwoRides() throws Exception {
    long first = create(), second = create();
    assertEquals(200, accept(first, 10));
    assertEquals(409, accept(second, 10));
    assertEquals(first, drivers.reservations.get(10L));
    assertEquals(RideStatus.SEARCHING, rides.findById(second).orElseThrow().getStatus());
  }

  @Test
  void noDriversRejectAllAndCancellation() throws Exception {
    drivers.empty = true;
    long none = create();
    assertEquals(RideStatus.NO_DRIVER_AVAILABLE, rides.findById(none).orElseThrow().getStatus());
    drivers.empty = false;
    long rejected = create();
    for (long d : List.of(10L, 20L))
      mvc.perform(post("/api/rides/" + rejected + "/reject").header("Authorization", driver(d)))
          .andExpect(status().isOk());
    assertEquals(
        RideStatus.NO_DRIVER_AVAILABLE, rides.findById(rejected).orElseThrow().getStatus());
    long cancelled = create();
    mvc.perform(post("/api/rides/" + cancelled + "/cancel").header("Authorization", passenger()))
        .andExpect(status().isOk());
    assertEquals(409, accept(cancelled, 10));
  }

  @Test
  void faresAndTransitions() {
    assertEquals(new BigDecimal("60.00"), fares.calculate(0));
    assertEquals(new BigDecimal("80.00"), fares.calculate(2));
    assertEquals(new BigDecimal("68.52"), fares.calculate(1.2345));
    assertThrows(IllegalArgumentException.class, () -> fares.calculate(Double.NaN));
    assertEquals(111.195, GeoDistance.kilometers(0, 0, 1, 0), 0.01);
    assertThrows(
        InvalidRideStatusException.class,
        () -> RideLifecycle.validateDriverTransition(RideStatus.COMPLETED, RideStatus.ARRIVING));
  }

  @Test
  void roleAndCoordinateValidation() throws Exception {
    mvc.perform(
            post("/api/rides")
                .header("Authorization", passenger())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
        .andExpect(status().isBadRequest());
    mvc.perform(get("/api/rides/driver/offers").header("Authorization", passenger()))
        .andExpect(status().isForbidden());
    mvc.perform(get("/api/rides/my")).andExpect(status().isUnauthorized());
  }
}
