package com.urbanglide.paymentservice;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.urbanglide.paymentservice.dto.CreatePaymentRequest;
import com.urbanglide.paymentservice.repository.PaymentRepository;
import com.urbanglide.paymentservice.service.PaymentService;
import java.math.BigDecimal;
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
class PaymentIntegrationTests {
  @Autowired MockMvc mvc;
  @Autowired PaymentRepository payments;
  @Autowired PaymentService service;

  @BeforeEach
  void clean() {
    payments.deleteAll();
  }

  @Test
  void paymentCreationAndCompletionAreIdempotent() throws Exception {
    var request = new CreatePaymentRequest(1L, 7L, new BigDecimal("90.00"));
    var payment = service.create(request);
    assertEquals(payment.paymentId(), service.create(request).paymentId());
    assertEquals(1, payments.count());
    mvc.perform(
            post("/api/payments/" + payment.paymentId() + "/complete")
                .header("Authorization", TestTokens.bearer(7, "PASSENGER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"paymentMethod\":\"UPI\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("paymentStatus").value("SUCCESS"));
    String reference =
        payments.findById(payment.paymentId()).orElseThrow().getTransactionReference();
    mvc.perform(
            post("/api/payments/" + payment.paymentId() + "/complete")
                .header("Authorization", TestTokens.bearer(7, "PASSENGER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"paymentMethod\":\"UPI\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("transactionReference").value(reference));
    mvc.perform(
            get("/api/payments/ride/1").header("Authorization", TestTokens.bearer(8, "PASSENGER")))
        .andExpect(status().isForbidden());
  }

  @Test
  void internalCreationIsProtectedAndValidated() throws Exception {
    mvc.perform(post("/internal/payments").contentType(MediaType.APPLICATION_JSON).content("{}"))
        .andExpect(status().isUnauthorized());
    mvc.perform(
            post("/internal/payments")
                .header("X-Internal-Key", "urbanglide-local-internal-key-change-me")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"rideId\":1,\"passengerId\":7,\"amount\":-1}"))
        .andExpect(status().isBadRequest());
    mvc.perform(
            post("/api/payments/999/complete")
                .header("Authorization", TestTokens.bearer(7, "PASSENGER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"paymentMethod\":\"CARD\"}"))
        .andExpect(status().isNotFound());
  }
}
