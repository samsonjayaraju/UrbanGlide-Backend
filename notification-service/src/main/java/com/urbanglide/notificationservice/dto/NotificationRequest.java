package com.urbanglide.notificationservice.dto;

import jakarta.validation.constraints.*;

public record NotificationRequest(
    @NotBlank @Size(max = 160) String eventKey,
    @NotNull @Positive Long recipientId,
    @Positive Long rideId,
    @NotBlank @Size(max = 40) String type,
    @NotBlank @Size(max = 500) String message) {}
