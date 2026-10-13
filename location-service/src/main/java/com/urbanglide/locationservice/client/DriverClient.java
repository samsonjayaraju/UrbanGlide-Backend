package com.urbanglide.locationservice.client;

import com.urbanglide.locationservice.dto.DriverIdentity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "driver-service", primary = false)
public interface DriverClient {
  @GetMapping("/internal/drivers/by-user/{id}")
  DriverIdentity byUser(@PathVariable("id") Long id);

  @GetMapping("/internal/drivers/{id}")
  DriverIdentity byId(@PathVariable("id") Long id);
}
