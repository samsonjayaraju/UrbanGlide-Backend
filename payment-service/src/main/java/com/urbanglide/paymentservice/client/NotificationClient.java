package com.urbanglide.paymentservice.client;

import com.urbanglide.paymentservice.dto.NotificationRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "notification-service", primary = false)
public interface NotificationClient {
  @PostMapping("/internal/notifications")
  void create(@RequestBody NotificationRequest request);
}
