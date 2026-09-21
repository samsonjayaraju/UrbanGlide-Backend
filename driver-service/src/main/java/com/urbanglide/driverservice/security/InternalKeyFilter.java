package com.urbanglide.driverservice.security;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

// Only /internal/** uses this service-to-service credential. User JWTs cannot access it.
public class InternalKeyFilter extends OncePerRequestFilter {
  private final String key;

  public InternalKeyFilter(String key) {
    this.key = key;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    if (request
        .getRequestURI()
        .substring(request.getContextPath().length())
        .startsWith("/internal/")) {
      String supplied = request.getHeader("X-Internal-Key");
      if (supplied == null
          || !MessageDigest.isEqual(
              key.getBytes(StandardCharsets.UTF_8), supplied.getBytes(StandardCharsets.UTF_8))) {
        response.setStatus(401);
        response.setContentType("application/json");
        response
            .getWriter()
            .write("{\"status\":401,\"message\":\"Valid internal service key required\"}");
        return;
      }
      var auth =
          new UsernamePasswordAuthenticationToken(
              "ride-service", null, List.of(new SimpleGrantedAuthority("ROLE_INTERNAL")));
      SecurityContextHolder.getContext().setAuthentication(auth);
    }
    chain.doFilter(request, response);
  }
}
