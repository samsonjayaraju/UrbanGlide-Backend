package com.urbanglide.rideservice;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;

final class TestTokens {
  static String bearer(long id, String role) {
    var key =
        new SecretKeySpec(
            "urbanglide-local-demo-secret-change-me-2026".getBytes(StandardCharsets.UTF_8),
            "HmacSHA256");
    var encoder = new NimbusJwtEncoder(new ImmutableSecret<>(key));
    Instant now = Instant.now();
    var claims =
        JwtClaimsSet.builder()
            .issuer("urbanglide-auth")
            .subject("" + id)
            .issuedAt(now)
            .expiresAt(now.plusSeconds(600))
            .claim("userId", id)
            .claim("email", "test" + id + "@example.com")
            .claim("role", role)
            .build();
    return "Bearer "
        + encoder
            .encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
            .getTokenValue();
  }
}
