package com.urbanglide.rideservice.dto;

import jakarta.validation.constraints.*;

public record RideRequestDto(
    @NotBlank @Size(max = 255) String pickupLocation,
    @NotNull @DecimalMin("-90") @DecimalMax("90") Double pickupLatitude,
    @NotNull @DecimalMin("-180") @DecimalMax("180") Double pickupLongitude,
    @NotBlank @Size(max = 255) String dropLocation,
    @NotNull @DecimalMin("-90") @DecimalMax("90") Double dropLatitude,
    @NotNull @DecimalMin("-180") @DecimalMax("180") Double dropLongitude) {}
