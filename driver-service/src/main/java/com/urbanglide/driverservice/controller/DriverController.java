package com.urbanglide.driverservice.controller;

import com.urbanglide.driverservice.dto.*;
import com.urbanglide.driverservice.service.DriverService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequestMapping("/api/drivers")
public class DriverController {
  private final DriverService drivers;

  public DriverController(DriverService drivers) {
    this.drivers = drivers;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public DriverResponse create(@Valid @RequestBody DriverProfileRequest request) {
    return drivers.create(request);
  }

  @GetMapping("/me")
  public DriverResponse me() {
    return drivers.me();
  }

  @GetMapping("/{id}")
  public DriverResponse get(@PathVariable @Positive Long id) {
    return drivers.get(id);
  }

  @PutMapping("/me")
  public DriverResponse update(@Valid @RequestBody DriverProfileRequest request) {
    return drivers.update(request);
  }

  @PatchMapping("/me/vehicle")
  public DriverResponse vehicle(@Valid @RequestBody VehicleRequest request) {
    return drivers.vehicle(request);
  }

  @PatchMapping("/me/location")
  public DriverResponse location(@Valid @RequestBody DriverLocationRequest request) {
    return drivers.location(request);
  }

  @PatchMapping("/me/availability")
  public DriverResponse availability(@Valid @RequestBody DriverAvailabilityRequest request) {
    return drivers.availability(request);
  }
}
