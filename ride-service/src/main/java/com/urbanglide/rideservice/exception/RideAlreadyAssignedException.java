package com.urbanglide.rideservice.exception;

import org.springframework.http.HttpStatus;

public class RideAlreadyAssignedException extends ApiException {
  public RideAlreadyAssignedException(String message) {
    super(HttpStatus.CONFLICT, message);
  }
}
