package com.urbanglide.paymentservice.security;

import com.urbanglide.paymentservice.exception.UnauthorizedActionException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

public final class CurrentUser {
  private CurrentUser() {}

  private static Jwt jwt() {
    return (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
  }

  public static Long id() {
    return ((Number) jwt().getClaim("userId")).longValue();
  }

  public static String role() {
    return jwt().getClaimAsString("role");
  }

  public static void requireRole(String role) {
    if (!role.equals(role())) throw new UnauthorizedActionException("Requires " + role + " role");
  }
}
