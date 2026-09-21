package com.urbanglide.driverservice.controller;

import com.urbanglide.driverservice.dto.*;
import com.urbanglide.driverservice.service.DriverService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequestMapping("/internal/drivers")
public class InternalDriverController {
  private final DriverService drivers;

  public InternalDriverController(DriverService drivers) {
    this.drivers = drivers;
  }

  @GetMapping("/nearby")
  public List<NearbyDriverResponse> nearby(
      @RequestParam @DecimalMin("-90") @DecimalMax("90") double pickupLatitude,
      @RequestParam @DecimalMin("-180") @DecimalMax("180") double pickupLongitude,
      @RequestParam @DecimalMin(value = "0", inclusive = false) @DecimalMax("100") double radius) {
    return drivers.nearby(pickupLatitude, pickupLongitude, radius);
  }

  @GetMapping("/by-user/{userId}")
  public DriverResponse byUser(@PathVariable @Positive Long userId) {
    return drivers.internalByUser(userId);
  }

  @PostMapping("/{id}/reserve")
  public void reserve(
      @PathVariable @Positive Long id, @Valid @RequestBody ReservationRequest request) {
    drivers.reserve(id, request.rideId());
  }

  @PostMapping("/{id}/release")
  public void release(
      @PathVariable @Positive Long id, @Valid @RequestBody ReservationRequest request) {
    drivers.release(id, request.rideId());
  }
}
