package com.urbanglide.locationservice.controller;

import com.urbanglide.locationservice.dto.*;
import com.urbanglide.locationservice.service.LocationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequestMapping("/internal/locations")
public class InternalLocationController {
  private final LocationService service;

  public InternalLocationController(LocationService service) {
    this.service = service;
  }

  @GetMapping("/drivers/{id}")
  public LocationResponse get(@PathVariable @Positive Long id) {
    return service.get(id);
  }

  @PutMapping("/drivers/{id}")
  public LocationResponse update(
      @PathVariable @Positive Long id, @Valid @RequestBody LocationRequest request) {
    return service.updateInternal(id, request);
  }

  @GetMapping("/nearby")
  public List<NearbyLocationResponse> nearby(
      @RequestParam @DecimalMin("-90") @DecimalMax("90") double pickupLatitude,
      @RequestParam @DecimalMin("-180") @DecimalMax("180") double pickupLongitude,
      @RequestParam @DecimalMin(value = "0", inclusive = false) @DecimalMax("100") double radius) {
    return service.nearby(pickupLatitude, pickupLongitude, radius);
  }
}
