package com.urbanglide.ratingservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.*;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RideDetails(Long rideId, Long passengerId, Long driverId, String status) {}
