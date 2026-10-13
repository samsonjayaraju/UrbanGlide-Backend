package com.urbanglide.ratingservice.controller;

import com.urbanglide.ratingservice.dto.*;
import com.urbanglide.ratingservice.service.ReviewService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequestMapping("/api/reviews")
public class ReviewController {
  private final ReviewService service;

  public ReviewController(ReviewService service) {
    this.service = service;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ReviewResponse create(@Valid @RequestBody ReviewRequest request) {
    return service.create(request);
  }

  @GetMapping("/my")
  public List<ReviewResponse> my(
      @RequestParam(defaultValue = "0") @Min(0) int page,
      @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
    return service.my(page, size);
  }

  @GetMapping("/drivers/{id}/summary")
  public RatingSummary summary(@PathVariable @Positive Long id) {
    return service.summary(id);
  }
}
