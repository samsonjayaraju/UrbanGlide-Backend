package com.urbanglide.driverservice.dto;

import jakarta.validation.constraints.*;

public record DriverProfileRequest(
    @NotBlank @Size(max = 100) String name,
    @NotBlank @Pattern(regexp = "[+0-9 ()-]{7,20}") String phone,
    @NotBlank @Size(max = 60) String licenseNumber,
    @NotBlank @Size(max = 40) String vehicleNumber,
    @NotBlank @Size(max = 80) String vehicleModel,
    @NotBlank @Size(max = 30) String vehicleType) {}
