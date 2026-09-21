package com.urbanglide.authservice.controller;

import com.urbanglide.authservice.dto.*;
import com.urbanglide.authservice.enums.Role;
import com.urbanglide.authservice.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
  private final AuthService auth;

  public AuthController(AuthService auth) {
    this.auth = auth;
  }

  @PostMapping("/register/passenger")
  @ResponseStatus(HttpStatus.CREATED)
  public UserResponse passenger(@Valid @RequestBody RegisterRequest request) {
    return auth.register(request, Role.PASSENGER);
  }

  @PostMapping("/register/driver")
  @ResponseStatus(HttpStatus.CREATED)
  public UserResponse driver(@Valid @RequestBody RegisterRequest request) {
    return auth.register(request, Role.DRIVER);
  }

  @PostMapping("/login")
  public LoginResponse login(@Valid @RequestBody LoginRequest request) {
    return auth.login(request);
  }

  @GetMapping("/me")
  public UserResponse me() {
    return auth.me();
  }
}
