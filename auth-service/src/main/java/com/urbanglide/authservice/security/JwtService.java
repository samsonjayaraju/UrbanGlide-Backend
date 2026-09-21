package com.urbanglide.authservice.security;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.urbanglide.authservice.entity.User;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
  private final JwtEncoder encoder;
  private final String issuer;
  private final long expiration;

  public JwtService(
      @Value("${security.jwt.secret}") String secret,
      @Value("${security.jwt.issuer}") String issuer,
      @Value("${security.jwt.expiration-seconds}") long expiration) {
    this.encoder =
        new NimbusJwtEncoder(
            new ImmutableSecret<>(
                new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256")));
    this.issuer = issuer;
    this.expiration = expiration;
  }

  public long getExpiration() {
    return expiration;
  }

  public String create(User user) {
    Instant now = Instant.now();
    var claims =
        JwtClaimsSet.builder()
            .issuer(issuer)
            .subject(user.getId().toString())
            .issuedAt(now)
            .expiresAt(now.plusSeconds(expiration))
            .claim("userId", user.getId())
            .claim("email", user.getEmail())
            .claim("role", user.getRole().name())
            .build();
    return encoder
        .encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
        .getTokenValue();
  }
}
