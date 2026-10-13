package com.urbanglide.driverservice.client;

import com.urbanglide.driverservice.dto.*;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "location-service", primary = false)
public interface LocationClient {
  @GetMapping("/internal/locations/drivers/{id}")
  LocationResponse get(@PathVariable("id") Long id);

  @PutMapping("/internal/locations/drivers/{id}")
  LocationResponse update(@PathVariable("id") Long id, @RequestBody DriverLocationRequest request);

  @GetMapping("/internal/locations/nearby")
  List<NearbyDriverResponse> nearby(
      @RequestParam("pickupLatitude") double lat,
      @RequestParam("pickupLongitude") double lon,
      @RequestParam("radius") double radius);
}
