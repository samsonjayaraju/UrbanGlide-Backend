package com.urbanglide.paymentservice.dto;

import com.urbanglide.paymentservice.enums.PaymentMethod;
import jakarta.validation.constraints.*;

public record CompletePaymentRequest(@NotNull PaymentMethod paymentMethod) {}
