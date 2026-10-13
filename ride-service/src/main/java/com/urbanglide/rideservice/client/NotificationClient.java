package com.urbanglide.rideservice.client;

import com.urbanglide.rideservice.dto.NotificationRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "notification-service", primary = false)
public interface NotificationClient {
  @PostMapping("/internal/notifications")
  void create(@RequestBody NotificationRequest request);
}
