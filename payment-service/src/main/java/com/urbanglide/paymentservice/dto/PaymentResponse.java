package com.urbanglide.paymentservice.dto;

import com.urbanglide.paymentservice.enums.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;

public record PaymentResponse(
    Long paymentId,
    Long rideId,
    Long passengerId,
    BigDecimal amount,
    PaymentMethod paymentMethod,
    PaymentStatus paymentStatus,
    String transactionReference,
    Instant createdAt) {}
