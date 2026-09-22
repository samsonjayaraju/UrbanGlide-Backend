package com.urbanglide.rideservice.dto;

import com.urbanglide.rideservice.enums.RideStatus;
import jakarta.validation.constraints.*;

public record RideStatusRequest(@NotNull RideStatus status) {}
