package com.urbanglide.driverservice.dto;

import jakarta.validation.constraints.*;

public record VehicleRequest(
    @NotBlank @Size(max = 40) String vehicleNumber,
    @NotBlank @Size(max = 80) String vehicleModel,
    @NotBlank @Size(max = 30) String vehicleType) {}
