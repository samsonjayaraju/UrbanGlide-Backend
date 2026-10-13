package com.urbanglide.ratingservice;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.urbanglide.ratingservice.client.*;
import com.urbanglide.ratingservice.dto.*;
import com.urbanglide.ratingservice.repository.ReviewRepository;
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
@Import(ReviewIntegrationTests.Clients.class)
class ReviewIntegrationTests {
  @TestConfiguration
  static class Clients {
    @Bean
    @Primary
    RideClient rides() {
      return id -> new RideDetails(id, 1L, 10L, id == 99 ? "IN_PROGRESS" : "COMPLETED");
    }

    @Bean
    @Primary
    DriverClient drivers() {
      return new DriverClient() {
        public DriverIdentity byUser(Long id) {
          return new DriverIdentity(10L, id);
        }

        public DriverIdentity byId(Long id) {
          return new DriverIdentity(id, 10L);
        }
      };
    }
  }

  @Autowired MockMvc mvc;
  @Autowired ReviewRepository reviews;

  @BeforeEach
  void clean() {
    reviews.deleteAll();
  }

  private void review(long ride, int rating, long user, int status) throws Exception {
    mvc.perform(
            post("/api/reviews")
                .header("Authorization", TestTokens.bearer(user, "PASSENGER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"rideId\":"
                        + ride
                        + ",\"rating\":"
                        + rating
                        + ",\"comment\":\"Good trip\"}"))
        .andExpect(status().is(status));
  }

  @Test
  void completedRideReviewsAndAverage() throws Exception {
    review(1, 5, 1, 201);
    review(2, 4, 1, 201);
    review(1, 3, 1, 409);
    mvc.perform(
            get("/api/reviews/drivers/10/summary")
                .header("Authorization", TestTokens.bearer(1, "PASSENGER")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("reviewCount").value(2))
        .andExpect(jsonPath("averageRating").value(4.5));
    mvc.perform(get("/api/reviews/my").header("Authorization", TestTokens.bearer(10, "DRIVER")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2));
  }

  @Test
  void ownershipEligibilityAndRatingBounds() throws Exception {
    review(1, 5, 2, 403);
    review(99, 5, 1, 409);
    review(1, 6, 1, 400);
    review(1, 0, 1, 400);
    mvc.perform(get("/api/reviews/my")).andExpect(status().isUnauthorized());
    mvc.perform(
            post("/api/reviews")
                .header("Authorization", TestTokens.bearer(10, "DRIVER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"rideId\":1,\"rating\":5,\"comment\":\"Good\"}"))
        .andExpect(status().isForbidden());
    mvc.perform(
            get("/internal/reviews/drivers/10/summary")
                .header("Authorization", TestTokens.bearer(1, "PASSENGER")))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void noReviewsHasZeroAverage() throws Exception {
    mvc.perform(
            get("/api/reviews/drivers/10/summary")
                .header("Authorization", TestTokens.bearer(1, "PASSENGER")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("reviewCount").value(0))
        .andExpect(jsonPath("averageRating").value(0.0));
  }
}
