package com.urbanglide.rideservice.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record CreatePaymentRequest(Long rideId, Long passengerId, BigDecimal amount) {}
