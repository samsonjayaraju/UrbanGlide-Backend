package com.urbanglide.authservice.service;

import com.urbanglide.authservice.dto.*;
import com.urbanglide.authservice.entity.User;
import com.urbanglide.authservice.enums.Role;
import com.urbanglide.authservice.exception.*;
import com.urbanglide.authservice.repository.UserRepository;
import com.urbanglide.authservice.security.*;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
  private final UserRepository users;
  private final PasswordEncoder passwords;
  private final JwtService jwt;

  public AuthService(UserRepository users, PasswordEncoder passwords, JwtService jwt) {
    this.users = users;
    this.passwords = passwords;
    this.jwt = jwt;
  }

  @Transactional
  public UserResponse register(RegisterRequest request, Role role) {
    String email = request.email().trim().toLowerCase(Locale.ROOT);
    if (users.existsByEmail(email))
      throw new ApiException(HttpStatus.CONFLICT, "Email already registered");
    if (request.password().getBytes(StandardCharsets.UTF_8).length > 72)
      throw new ApiException(HttpStatus.BAD_REQUEST, "Password must be at most 72 UTF-8 bytes");
    User user = new User();
    user.setName(request.name().trim());
    user.setEmail(email);
    user.setPhone(request.phone());
    user.setPassword(passwords.encode(request.password()));
    user.setRole(role);
    return response(users.saveAndFlush(user));
  }

  public LoginResponse login(LoginRequest request) {
    User user =
        users
            .findByEmail(request.email().trim().toLowerCase(Locale.ROOT))
            .filter(u -> passwords.matches(request.password(), u.getPassword()))
            .orElseThrow(
                () -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));
    return new LoginResponse(jwt.create(user), "Bearer", jwt.getExpiration(), response(user));
  }

  public UserResponse me() {
    return response(
        users
            .findById(CurrentUser.id())
            .orElseThrow(() -> new ResourceNotFoundException("User not found")));
  }

  private UserResponse response(User user) {
    return new UserResponse(
        user.getId(), user.getName(), user.getEmail(), user.getPhone(), user.getRole());
  }
}
