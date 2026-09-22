package com.urbanglide.rideservice.dto;

import com.urbanglide.rideservice.enums.RideStatus;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;

public record RideResponseDto(
    Long rideId,
    Long passengerId,
    Long driverId,
    String pickupLocation,
    Double pickupLatitude,
    Double pickupLongitude,
    String dropLocation,
    Double dropLatitude,
    Double dropLongitude,
    Double distance,
    BigDecimal fare,
    RideStatus status,
    Instant createdAt,
    Instant completedAt,
    boolean completionSynced) {}
