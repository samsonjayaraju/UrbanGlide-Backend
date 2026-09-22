package com.urbanglide.rideservice.dto;

import com.urbanglide.rideservice.enums.RideOfferStatus;
import jakarta.validation.constraints.*;
import java.time.Instant;

public record RideOfferResponse(
    Long offerId,
    Long rideId,
    Long driverId,
    RideOfferStatus status,
    Instant createdAt,
    RideResponseDto ride) {}
