package com.urbanglide.paymentservice.controller;

import com.urbanglide.paymentservice.dto.*;
import com.urbanglide.paymentservice.service.PaymentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequestMapping("/api/payments")
public class PaymentController {
  private final PaymentService payments;

  public PaymentController(PaymentService payments) {
    this.payments = payments;
  }

  @GetMapping("/my")
  public List<PaymentResponse> my() {
    return payments.my();
  }

  @GetMapping("/ride/{rideId}")
  public PaymentResponse byRide(@PathVariable @Positive Long rideId) {
    return payments.byRide(rideId);
  }

  @GetMapping("/{id}")
  public PaymentResponse get(@PathVariable @Positive Long id) {
    return payments.get(id);
  }

  @PostMapping("/{id}/complete")
  public PaymentResponse complete(
      @PathVariable @Positive Long id, @Valid @RequestBody CompletePaymentRequest request) {
    return payments.complete(id, request);
  }
}
