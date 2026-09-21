package com.urbanglide.driverservice;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.urbanglide.driverservice.exception.ApiException;
import com.urbanglide.driverservice.repository.DriverRepository;
import com.urbanglide.driverservice.service.DriverService;
import com.urbanglide.driverservice.service.GeoDistance;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DriverIntegrationTests {
  @Autowired MockMvc mvc;
  @Autowired DriverRepository drivers;
  @Autowired DriverService service;

  @BeforeEach
  void clean() {
    drivers.deleteAll();
  }

  private String token(long id) {
    return TestTokens.bearer(id, "DRIVER");
  }

  private long create(long user, double lat) throws Exception {
    mvc.perform(
            post("/api/drivers")
                .header("Authorization", token(user))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
{"name":"Driver","phone":"9876543210","licenseNumber":"LIC%s","vehicleNumber":"AP%s","vehicleModel":"Swift","vehicleType":"CAR"}
"""
                        .formatted(user, user)))
        .andExpect(status().isCreated());
    mvc.perform(
            patch("/api/drivers/me/location")
                .header("Authorization", token(user))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"latitude\":" + lat + ",\"longitude\":79.9865}"))
        .andExpect(status().isOk());
    mvc.perform(
            patch("/api/drivers/me/availability")
                .header("Authorization", token(user))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"availability\":\"AVAILABLE\"}"))
        .andExpect(status().isOk());
    return drivers.findByUserId(user).orElseThrow().getDriverId();
  }

  @Test
  void nearbyDriversAreSortedAndBusyDriversExcluded() throws Exception {
    long near = create(1, 14.4426);
    create(2, 14.46);
    create(3, 15.0);
    var nearby = service.nearby(14.4426, 79.9865, 5);
    assertEquals(2, nearby.size());
    assertEquals(near, nearby.getFirst().driverId());
    service.reserve(near, 99L);
    service.reserve(near, 99L);
    assertEquals(1, service.nearby(14.4426, 79.9865, 5).size());
    assertThrows(ApiException.class, () -> service.reserve(near, 100L));
    service.release(near, 100L);
    assertEquals(99L, drivers.findById(near).orElseThrow().getActiveRideId());
    mvc.perform(
            patch("/api/drivers/me/availability")
                .header("Authorization", token(1))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"availability\":\"AVAILABLE\"}"))
        .andExpect(status().isConflict());
    service.release(near, 99L);
    assertEquals(2, service.nearby(14.4426, 79.9865, 5).size());
  }

  @Test
  void validationOwnershipAndInternalAuthentication() throws Exception {
    long id = create(1, 14.4426);
    mvc.perform(get("/api/drivers/" + id).header("Authorization", token(2)))
        .andExpect(status().isForbidden());
    mvc.perform(
            patch("/api/drivers/me/location")
                .header("Authorization", token(1))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"latitude\":91,\"longitude\":80}"))
        .andExpect(status().isBadRequest());
    mvc.perform(get("/internal/drivers/by-user/1").header("Authorization", token(1)))
        .andExpect(status().isUnauthorized());
    mvc.perform(
            get("/internal/drivers/by-user/1")
                .header("X-Internal-Key", "urbanglide-local-internal-key-change-me"))
        .andExpect(status().isOk());
  }

  @Test
  void haversineKnownDistances() {
    assertEquals(0, GeoDistance.kilometers(0, 0, 0, 0), 0.0001);
    assertEquals(111.195, GeoDistance.kilometers(0, 0, 1, 0), 0.01);
    assertEquals(20015.114, GeoDistance.kilometers(0, 0, 0, 180), 0.01);
  }
}
