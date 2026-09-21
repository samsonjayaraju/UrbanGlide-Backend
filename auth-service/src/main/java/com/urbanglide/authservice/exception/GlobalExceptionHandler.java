package com.urbanglide.authservice.exception;

import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {
  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  public record ErrorResponse(
      Instant timestamp, int status, String message, Map<String, String> errors) {}

  private ResponseEntity<ErrorResponse> error(
      HttpStatus status, String message, Map<String, String> errors) {
    return ResponseEntity.status(status)
        .body(new ErrorResponse(Instant.now(), status.value(), message, errors));
  }

  @ExceptionHandler(ApiException.class)
  ResponseEntity<ErrorResponse> api(ApiException ex) {
    return error(ex.getStatus(), ex.getMessage(), Map.of());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  ResponseEntity<ErrorResponse> validation(MethodArgumentNotValidException ex) {
    Map<String, String> fields = new LinkedHashMap<>();
    ex.getBindingResult()
        .getFieldErrors()
        .forEach(e -> fields.put(e.getField(), e.getDefaultMessage()));
    return error(HttpStatus.BAD_REQUEST, "Validation failed", fields);
  }

  @ExceptionHandler({
    HttpMessageNotReadableException.class,
    ConstraintViolationException.class,
    MethodArgumentTypeMismatchException.class
  })
  ResponseEntity<ErrorResponse> badRequest(Exception ex) {
    return error(HttpStatus.BAD_REQUEST, "Invalid request values or JSON", Map.of());
  }

  @ExceptionHandler({DataIntegrityViolationException.class, ConcurrencyFailureException.class})
  ResponseEntity<ErrorResponse> conflict(Exception ex) {
    return error(
        HttpStatus.CONFLICT, "Record already exists or was changed by another request", Map.of());
  }

  @ExceptionHandler(AccessDeniedException.class)
  ResponseEntity<ErrorResponse> denied(Exception ex) {
    return error(HttpStatus.FORBIDDEN, "Action not permitted", Map.of());
  }

  @ExceptionHandler(Exception.class)
  ResponseEntity<ErrorResponse> unexpected(Exception ex) {
    log.error("Unexpected request failure", ex);
    return error(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected server error", Map.of());
  }
}
