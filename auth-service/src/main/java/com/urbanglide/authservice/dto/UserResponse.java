package com.urbanglide.authservice.dto;

import com.urbanglide.authservice.enums.Role;
import jakarta.validation.constraints.*;

public record UserResponse(Long id, String name, String email, String phone, Role role) {}
