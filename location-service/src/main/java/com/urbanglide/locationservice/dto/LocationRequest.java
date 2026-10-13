package com.urbanglide.locationservice.dto;

import jakarta.validation.constraints.*;

public record LocationRequest(
    @NotNull @DecimalMin("-90") @DecimalMax("90") Double latitude,
    @NotNull @DecimalMin("-180") @DecimalMax("180") Double longitude) {}
