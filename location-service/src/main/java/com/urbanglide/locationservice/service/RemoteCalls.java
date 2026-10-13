package com.urbanglide.locationservice.service;

import com.urbanglide.locationservice.exception.*;
import feign.FeignException;
import java.util.function.Supplier;
import org.springframework.http.HttpStatus;

public final class RemoteCalls {
  private RemoteCalls() {}

  public static <T> T call(Supplier<T> action) {
    try {
      return action.get();
    } catch (FeignException ex) {
      if (ex.status() == 404)
        throw new ResourceNotFoundException("Required driver, location or ride was not found");
      if (ex.status() == 409)
        throw new ApiException(HttpStatus.CONFLICT, "Remote resource conflicts with this request");
      throw new ApiException(
          HttpStatus.SERVICE_UNAVAILABLE,
          "A required service is unavailable; retry when it recovers");
    }
  }
}
