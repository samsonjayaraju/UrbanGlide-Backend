package com.urbanglide.locationservice.controller;

import com.urbanglide.locationservice.dto.*;
import com.urbanglide.locationservice.service.LocationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequestMapping("/api/locations")
public class LocationController {
  private final LocationService service;

  public LocationController(LocationService service) {
    this.service = service;
  }

  @PatchMapping("/me")
  public LocationResponse update(@Valid @RequestBody LocationRequest request) {
    return service.updateMine(request);
  }

  @GetMapping("/me")
  public LocationResponse mine() {
    return service.mine();
  }

  @GetMapping("/rides/{id}")
  public LocationResponse track(@PathVariable @Positive Long id) {
    return service.forRide(id);
  }
}
