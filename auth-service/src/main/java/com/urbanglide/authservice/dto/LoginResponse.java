package com.urbanglide.authservice.dto;

import jakarta.validation.constraints.*;

public record LoginResponse(String token, String tokenType, long expiresIn, UserResponse user) {}
