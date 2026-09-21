package com.urbanglide.driverservice.dto;

import jakarta.validation.constraints.*;

public record NearbyDriverResponse(Long driverId, double distanceKm) {}
