package com.urbanglide.rideservice.dto;

import jakarta.validation.constraints.*;

public record NearbyDriverResponse(Long driverId, double distanceKm) {}
