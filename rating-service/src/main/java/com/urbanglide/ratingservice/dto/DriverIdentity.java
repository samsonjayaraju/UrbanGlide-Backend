package com.urbanglide.ratingservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.*;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DriverIdentity(Long driverId, Long userId) {}
