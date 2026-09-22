package com.urbanglide.rideservice.config;

import feign.Request;
import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;

@Configuration
public class FeignConfig {
  @Bean
  RequestInterceptor internalKey(@Value("${security.internal-key}") String key) {
    return template -> template.header("X-Internal-Key", key);
  }

  @Bean
  Request.Options timeouts() {
    return new Request.Options(
        java.time.Duration.ofSeconds(3), java.time.Duration.ofSeconds(5), true);
  }
}
