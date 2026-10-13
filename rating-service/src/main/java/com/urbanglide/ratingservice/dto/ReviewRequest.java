package com.urbanglide.ratingservice.dto;

import jakarta.validation.constraints.*;

public record ReviewRequest(
    @NotNull @Positive Long rideId,
    @NotNull @Min(1) @Max(5) Integer rating,
    @NotNull @Size(max = 1000) String comment) {}
