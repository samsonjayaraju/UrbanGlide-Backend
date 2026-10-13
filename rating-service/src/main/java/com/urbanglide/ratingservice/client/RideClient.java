package com.urbanglide.ratingservice.client;

import com.urbanglide.ratingservice.dto.RideDetails;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "ride-service", primary = false)
public interface RideClient {
  @GetMapping("/internal/rides/{id}")
  RideDetails get(@PathVariable("id") Long id);
}
