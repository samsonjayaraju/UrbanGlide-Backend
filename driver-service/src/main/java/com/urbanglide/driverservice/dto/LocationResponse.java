package com.urbanglide.driverservice.dto;

import jakarta.validation.constraints.*;
import java.time.Instant;

public record LocationResponse(
    Long driverId, Double latitude, Double longitude, Instant updatedAt, boolean stale) {}
