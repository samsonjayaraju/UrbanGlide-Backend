package com.urbanglide.ratingservice.controller;

import com.urbanglide.ratingservice.dto.RatingSummary;
import com.urbanglide.ratingservice.service.ReviewService;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequestMapping("/internal/reviews")
public class InternalReviewController {
  private final ReviewService service;

  public InternalReviewController(ReviewService service) {
    this.service = service;
  }

  @GetMapping("/drivers/{id}/summary")
  public RatingSummary summary(@PathVariable @Positive Long id) {
    return service.internalSummary(id);
  }
}
