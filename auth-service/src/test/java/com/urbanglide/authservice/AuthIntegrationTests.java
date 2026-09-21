package com.urbanglide.authservice;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.urbanglide.authservice.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthIntegrationTests {
  @Autowired MockMvc mvc;
  @Autowired ObjectMapper json;
  @Autowired UserRepository users;
  @Autowired PasswordEncoder passwords;

  @BeforeEach
  void clean() {
    users.deleteAll();
  }

  @Test
  void registerLoginAndReadOwnProfile() throws Exception {
    mvc.perform(
            post("/api/auth/register/passenger")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
{"name":"Passenger","email":"PERSON@example.com","password":"Password123!","phone":"9876543210"}
"""))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("role").value("PASSENGER"))
        .andExpect(jsonPath("password").doesNotExist());
    var stored = users.findByEmail("person@example.com").orElseThrow();
    assertNotEquals("Password123!", stored.getPassword());
    assertTrue(passwords.matches("Password123!", stored.getPassword()));
    String login =
        mvc.perform(
                post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {"email":"person@example.com","password":"Password123!"}
                        """))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    String token = json.readTree(login).get("token").asText();
    mvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("email").value("person@example.com"));
    mvc.perform(get("/api/auth/me")).andExpect(status().isUnauthorized());
    mvc.perform(get("/api/auth/me").header("Authorization", "Bearer broken"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void invalidLoginValidationAndDuplicateEmail() throws Exception {
    String body =
        """
{"name":"Driver","email":"driver@example.com","password":"Password123!","phone":"9876543210"}
""";
    mvc.perform(
            post("/api/auth/register/driver").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("role").value("DRIVER"));
    mvc.perform(
            post("/api/auth/register/driver").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isConflict());
    mvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"email":"driver@example.com","password":"wrong"}
                    """))
        .andExpect(status().isUnauthorized());
    mvc.perform(
            post("/api/auth/register/passenger")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
        .andExpect(status().isBadRequest());
    mvc.perform(
            post("/api/auth/register/passenger")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body.replace("\"phone\"", "\"role\":\"ADMIN\",\"phone\"")))
        .andExpect(status().isBadRequest());
  }

  @Test
  void expiredAndIncorrectlySignedTokensAreRejected() throws Exception {
    var key =
        new javax.crypto.spec.SecretKeySpec(
            "urbanglide-local-demo-secret-change-me-2026"
                .getBytes(java.nio.charset.StandardCharsets.UTF_8),
            "HmacSHA256");
    var encoder =
        new org.springframework.security.oauth2.jwt.NimbusJwtEncoder(
            new com.nimbusds.jose.jwk.source.ImmutableSecret<>(key));
    var now = java.time.Instant.now();
    var claims =
        org.springframework.security.oauth2.jwt.JwtClaimsSet.builder()
            .issuer("urbanglide-auth")
            .subject("1")
            .issuedAt(now.minusSeconds(600))
            .expiresAt(now.minusSeconds(120))
            .claim("userId", 1L)
            .claim("email", "test@example.com")
            .claim("role", "PASSENGER")
            .build();
    String expired =
        encoder
            .encode(
                org.springframework.security.oauth2.jwt.JwtEncoderParameters.from(
                    org.springframework.security.oauth2.jwt.JwsHeader.with(
                            org.springframework.security.oauth2.jose.jws.MacAlgorithm.HS256)
                        .build(),
                    claims))
            .getTokenValue();
    mvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + expired))
        .andExpect(status().isUnauthorized());
    String[] parts = expired.split("\\.");
    String tampered = parts[0] + "." + parts[1] + "." + "A".repeat(43);
    mvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + tampered))
        .andExpect(status().isUnauthorized());
  }
}
