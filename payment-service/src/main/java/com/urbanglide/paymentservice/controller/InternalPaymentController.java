package com.urbanglide.paymentservice.controller;

import com.urbanglide.paymentservice.dto.*;
import com.urbanglide.paymentservice.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/payments")
public class InternalPaymentController {
  private final PaymentService payments;

  public InternalPaymentController(PaymentService payments) {
    this.payments = payments;
  }

  @PostMapping
  public PaymentResponse create(@Valid @RequestBody CreatePaymentRequest request) {
    return payments.create(request);
  }
}
