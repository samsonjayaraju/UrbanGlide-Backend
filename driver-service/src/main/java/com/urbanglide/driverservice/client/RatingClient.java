package com.urbanglide.driverservice.client;

import com.urbanglide.driverservice.dto.RatingSummary;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "rating-service", primary = false)
public interface RatingClient {
  @GetMapping("/internal/reviews/drivers/{id}/summary")
  RatingSummary summary(@PathVariable("id") Long id);
}
