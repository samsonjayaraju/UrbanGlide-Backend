package com.urbanglide.paymentservice.dto;

import jakarta.validation.constraints.*;

public record NotificationRequest(
    String eventKey, Long recipientId, Long rideId, String type, String message) {}
