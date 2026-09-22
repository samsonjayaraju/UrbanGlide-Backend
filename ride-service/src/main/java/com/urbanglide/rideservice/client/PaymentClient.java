package com.urbanglide.rideservice.client;

import com.urbanglide.rideservice.dto.CreatePaymentRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(primary = false, name = "payment-service")
public interface PaymentClient {
  @PostMapping("/internal/payments")
  void create(@RequestBody CreatePaymentRequest request);
}
