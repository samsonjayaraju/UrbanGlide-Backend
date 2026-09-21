package com.urbanglide.paymentservice.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedActionException extends ApiException {
  public UnauthorizedActionException(String message) {
    super(HttpStatus.FORBIDDEN, message);
  }
}
