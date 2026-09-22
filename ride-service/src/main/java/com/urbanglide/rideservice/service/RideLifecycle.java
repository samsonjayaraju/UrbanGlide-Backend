package com.urbanglide.rideservice.service;

import com.urbanglide.rideservice.enums.RideStatus;
import com.urbanglide.rideservice.exception.InvalidRideStatusException;

public final class RideLifecycle {
  private RideLifecycle() {}

  public static void validateDriverTransition(RideStatus current, RideStatus next) {
    boolean valid =
        switch (current) {
          case DRIVER_ASSIGNED -> next == RideStatus.ARRIVING;
          case ARRIVING -> next == RideStatus.ARRIVED;
          case ARRIVED -> next == RideStatus.IN_PROGRESS;
          case IN_PROGRESS -> next == RideStatus.COMPLETED;
          default -> false;
        };
    if (!valid)
      throw new InvalidRideStatusException("Cannot change ride from " + current + " to " + next);
  }
}
