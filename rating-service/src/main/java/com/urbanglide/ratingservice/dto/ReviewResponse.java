package com.urbanglide.ratingservice.dto;

import jakarta.validation.constraints.*;
import java.time.Instant;

public record ReviewResponse(
    Long reviewId, Long rideId, Long driverId, Integer rating, String comment, Instant createdAt) {}
