package com.urbanglide.paymentservice.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record CreatePaymentRequest(
    @NotNull @Positive Long rideId,
    @NotNull @Positive Long passengerId,
    @NotNull @DecimalMin("0.01") @Digits(integer = 10, fraction = 2) BigDecimal amount) {}
