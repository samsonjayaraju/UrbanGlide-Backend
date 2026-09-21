package com.urbanglide.driverservice.dto;

import jakarta.validation.constraints.*;

public record DriverLocationRequest(
    @NotNull @DecimalMin("-90") @DecimalMax("90") Double latitude,
    @NotNull @DecimalMin("-180") @DecimalMax("180") Double longitude) {}
