package com.urbanglide.locationservice.dto;

import jakarta.validation.constraints.*;

public record NearbyLocationResponse(Long driverId, double distanceKm) {}
