package com.urbanglide.rideservice.controller;

import com.urbanglide.rideservice.dto.*;
import com.urbanglide.rideservice.service.RideService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequestMapping("/api/rides")
public class RideController {
  private final RideService rides;

  public RideController(RideService rides) {
    this.rides = rides;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public RideResponseDto create(@Valid @RequestBody RideRequestDto request) {
    return rides.create(request);
  }

  @GetMapping("/my")
  public List<RideResponseDto> my() {
    return rides.my();
  }

  @GetMapping("/driver/my")
  public List<RideResponseDto> driverHistory() {
    com.urbanglide.rideservice.security.CurrentUser.requireRole("DRIVER");
    return rides.my();
  }

  @GetMapping("/driver/offers")
  public List<RideOfferResponse> offers() {
    return rides.pendingOffers();
  }

  @GetMapping("/{id}")
  public RideResponseDto get(@PathVariable @Positive Long id) {
    return rides.get(id);
  }

  @PostMapping("/{id}/accept")
  public RideResponseDto accept(@PathVariable @Positive Long id) {
    return rides.accept(id);
  }

  @PostMapping("/{id}/reject")
  public RideResponseDto reject(@PathVariable @Positive Long id) {
    return rides.reject(id);
  }

  @PostMapping("/{id}/cancel")
  public RideResponseDto cancel(@PathVariable @Positive Long id) {
    return rides.cancel(id);
  }

  @PatchMapping("/{id}/status")
  public RideResponseDto status(
      @PathVariable @Positive Long id, @Valid @RequestBody RideStatusRequest request) {
    return rides.updateStatus(id, request.status());
  }
}
