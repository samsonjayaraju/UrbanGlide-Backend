package com.urbanglide.rideservice.dto;

import jakarta.validation.constraints.*;

public record NotificationRequest(
    String eventKey, Long recipientId, Long rideId, String type, String message) {}
