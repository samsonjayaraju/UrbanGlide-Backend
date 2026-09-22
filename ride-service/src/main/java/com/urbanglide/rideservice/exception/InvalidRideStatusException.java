package com.urbanglide.rideservice.exception;

import org.springframework.http.HttpStatus;

public class InvalidRideStatusException extends ApiException {
  public InvalidRideStatusException(String message) {
    super(HttpStatus.BAD_REQUEST, message);
  }
}
