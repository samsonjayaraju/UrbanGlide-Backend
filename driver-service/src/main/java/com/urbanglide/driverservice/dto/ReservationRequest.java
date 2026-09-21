package com.urbanglide.driverservice.dto;

import jakarta.validation.constraints.*;

public record ReservationRequest(@NotNull @Positive Long rideId) {}
