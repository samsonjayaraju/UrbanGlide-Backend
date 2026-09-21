package com.urbanglide.driverservice.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedActionException extends ApiException {
  public UnauthorizedActionException(String message) {
    super(HttpStatus.FORBIDDEN, message);
  }
}
