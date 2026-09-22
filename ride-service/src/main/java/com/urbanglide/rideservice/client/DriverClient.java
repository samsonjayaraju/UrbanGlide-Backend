package com.urbanglide.rideservice.client;

import com.urbanglide.rideservice.dto.*;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(primary = false, name = "driver-service")
public interface DriverClient {
  @GetMapping("/internal/drivers/nearby")
  List<NearbyDriverResponse> nearby(
      @RequestParam("pickupLatitude") double latitude,
      @RequestParam("pickupLongitude") double longitude,
      @RequestParam("radius") double radius);

  @GetMapping("/internal/drivers/by-user/{userId}")
  DriverIdentity byUser(@PathVariable("userId") Long userId);

  @PostMapping("/internal/drivers/{id}/reserve")
  void reserve(@PathVariable("id") Long driverId, @RequestBody ReservationRequest request);

  @PostMapping("/internal/drivers/{id}/release")
  void release(@PathVariable("id") Long driverId, @RequestBody ReservationRequest request);
}
