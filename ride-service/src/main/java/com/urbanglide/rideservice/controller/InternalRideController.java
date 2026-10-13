package com.urbanglide.rideservice.controller;

import com.urbanglide.rideservice.dto.RideResponseDto;
import com.urbanglide.rideservice.service.RideService;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequestMapping("/internal/rides")
public class InternalRideController {
  private final RideService rides;

  public InternalRideController(RideService rides) {
    this.rides = rides;
  }

  @GetMapping("/{id}")
  public RideResponseDto get(@PathVariable @Positive Long id) {
    return rides.internalGet(id);
  }
}
