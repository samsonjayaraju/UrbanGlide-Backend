package com.urbanglide.rideservice.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedActionException extends ApiException {
  public UnauthorizedActionException(String message) {
    super(HttpStatus.FORBIDDEN, message);
  }
}
