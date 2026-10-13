package com.urbanglide.locationservice;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.urbanglide.locationservice.client.*;
import com.urbanglide.locationservice.dto.*;
import com.urbanglide.locationservice.repository.DriverLocationRepository;
import com.urbanglide.locationservice.service.*;
import java.time.Instant;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(LocationIntegrationTests.Clients.class)
class LocationIntegrationTests {
  @TestConfiguration
  static class Clients {
    @Bean
    @Primary
    RideClient rides() {
      return id -> new RideDetails(id, 1L, 10L, id == 99 ? "COMPLETED" : "IN_PROGRESS");
    }

    @Bean
    @Primary
    DriverClient drivers() {
      return new DriverClient() {
        public DriverIdentity byUser(Long id) {
          return new DriverIdentity(id, id);
        }

        public DriverIdentity byId(Long id) {
          return new DriverIdentity(id, id);
        }
      };
    }
  }

  @Autowired MockMvc mvc;
  @Autowired DriverLocationRepository locations;
  @Autowired LocationService service;

  @BeforeEach
  void clean() {
    locations.deleteAll();
  }

  private void update(long user, double lat) throws Exception {
    mvc.perform(
            patch("/api/locations/me")
                .header("Authorization", TestTokens.bearer(user, "DRIVER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"latitude\":" + lat + ",\"longitude\":79.9865}"))
        .andExpect(status().isOk());
  }

  @Test
  void updatesMatchingFreshnessAndSorting() throws Exception {
    update(10, 14.4426);
    update(20, 14.46);
    update(30, 15);
    update(10, 14.443);
    assertEquals(3, locations.count());
    var found = service.nearby(14.4426, 79.9865, 5);
    assertEquals(2, found.size());
    assertEquals(10L, found.getFirst().driverId());
    var stale = locations.findById(10L).orElseThrow();
    stale.setUpdatedAt(Instant.now().minusSeconds(600));
    locations.save(stale);
    assertTrue(service.get(10L).stale());
    assertEquals(1, service.nearby(14.4426, 79.9865, 5).size());
  }

  @Test
  void trackingRequiresActiveRideAndParticipant() throws Exception {
    update(10, 14.4426);
    mvc.perform(
            get("/api/locations/rides/1")
                .header("Authorization", TestTokens.bearer(1, "PASSENGER")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("driverId").value(10));
    mvc.perform(
            get("/api/locations/rides/1")
                .header("Authorization", TestTokens.bearer(2, "PASSENGER")))
        .andExpect(status().isForbidden());
    mvc.perform(
            get("/api/locations/rides/1").header("Authorization", TestTokens.bearer(20, "DRIVER")))
        .andExpect(status().isForbidden());
    mvc.perform(
            get("/api/locations/rides/99")
                .header("Authorization", TestTokens.bearer(1, "PASSENGER")))
        .andExpect(status().isConflict());
  }

  @Test
  void validationRolesAndInternalKey() throws Exception {
    mvc.perform(
            patch("/api/locations/me")
                .header("Authorization", TestTokens.bearer(10, "DRIVER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"latitude\":91,\"longitude\":79}"))
        .andExpect(status().isBadRequest());
    mvc.perform(
            patch("/api/locations/me")
                .header("Authorization", TestTokens.bearer(1, "PASSENGER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"latitude\":14,\"longitude\":79}"))
        .andExpect(status().isForbidden());
    mvc.perform(
            get("/internal/locations/drivers/10")
                .header("Authorization", TestTokens.bearer(10, "DRIVER")))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void haversineReferenceDistances() {
    assertEquals(0, GeoDistance.kilometers(0, 0, 0, 0), 0.0001);
    assertEquals(111.195, GeoDistance.kilometers(0, 0, 1, 0), 0.01);
    assertEquals(20015.114, GeoDistance.kilometers(0, 0, 0, 180), 0.01);
  }
}
