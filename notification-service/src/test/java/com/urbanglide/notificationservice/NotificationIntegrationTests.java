package com.urbanglide.notificationservice;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.urbanglide.notificationservice.dto.NotificationRequest;
import com.urbanglide.notificationservice.repository.NotificationRepository;
import com.urbanglide.notificationservice.service.NotificationService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class NotificationIntegrationTests {
  @Autowired MockMvc mvc;
  @Autowired NotificationRepository notifications;
  @Autowired NotificationService service;

  @BeforeEach
  void clean() {
    notifications.deleteAll();
  }

  @Test
  void privateInboxReadAndDuplicateDelivery() throws Exception {
    var request =
        new NotificationRequest("ride:10:ARRIVED", 1L, 10L, "RIDE_STATUS", "Driver arrived");
    var n = service.create(request);
    assertEquals(n.notificationId(), service.create(request).notificationId());
    assertEquals(1, notifications.count());
    mvc.perform(
            get("/api/notifications/my").header("Authorization", TestTokens.bearer(1, "PASSENGER")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].type").value("RIDE_STATUS"));
    mvc.perform(
            get("/api/notifications/my").header("Authorization", TestTokens.bearer(2, "PASSENGER")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(0));
    mvc.perform(
            patch("/api/notifications/" + n.notificationId() + "/read")
                .header("Authorization", TestTokens.bearer(2, "PASSENGER")))
        .andExpect(status().isForbidden());
    mvc.perform(
            patch("/api/notifications/" + n.notificationId() + "/read")
                .header("Authorization", TestTokens.bearer(1, "PASSENGER")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("readAt").isNotEmpty());
    mvc.perform(
            get("/api/notifications/unread-count")
                .header("Authorization", TestTokens.bearer(1, "PASSENGER")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("unreadCount").value(0));
  }

  @Test
  void internalAuthenticationAndValidation() throws Exception {
    mvc.perform(get("/api/notifications/my")).andExpect(status().isUnauthorized());
    mvc.perform(
            post("/internal/notifications")
                .header("Authorization", TestTokens.bearer(1, "PASSENGER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
        .andExpect(status().isUnauthorized());
    mvc.perform(
            post("/internal/notifications")
                .header("X-Internal-Key", "urbanglide-local-internal-key-change-me")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
        .andExpect(status().isBadRequest());
    mvc.perform(
            get("/api/notifications/my?size=1000")
                .header("Authorization", TestTokens.bearer(1, "PASSENGER")))
        .andExpect(status().isBadRequest());
  }
}
