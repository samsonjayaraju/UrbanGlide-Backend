package com.urbanglide.notificationservice.dto;

import jakarta.validation.constraints.*;
import java.time.Instant;

public record NotificationResponse(
    Long notificationId,
    Long rideId,
    String type,
    String message,
    Instant createdAt,
    Instant readAt) {}
