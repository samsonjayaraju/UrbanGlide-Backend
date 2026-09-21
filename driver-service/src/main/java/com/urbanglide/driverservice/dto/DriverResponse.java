package com.urbanglide.driverservice.dto;

import com.urbanglide.driverservice.enums.DriverAvailability;
import jakarta.validation.constraints.*;

public record DriverResponse(
    Long driverId,
    Long userId,
    String name,
    String phone,
    String licenseNumber,
    String vehicleNumber,
    String vehicleModel,
    String vehicleType,
    DriverAvailability availability,
    Double latitude,
    Double longitude,
    Double rating) {}
