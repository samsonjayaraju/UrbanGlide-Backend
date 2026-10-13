package com.urbanglide.ratingservice.dto;

import jakarta.validation.constraints.*;

public record RatingSummary(Long driverId, long reviewCount, double averageRating) {}
