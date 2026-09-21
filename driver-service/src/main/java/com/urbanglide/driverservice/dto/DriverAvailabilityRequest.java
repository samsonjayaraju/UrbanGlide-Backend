package com.urbanglide.driverservice.dto;

import com.urbanglide.driverservice.enums.DriverAvailability;
import jakarta.validation.constraints.*;

public record DriverAvailabilityRequest(@NotNull DriverAvailability availability) {}
