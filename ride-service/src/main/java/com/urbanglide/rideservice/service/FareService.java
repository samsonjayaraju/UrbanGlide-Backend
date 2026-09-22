package com.urbanglide.rideservice.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class FareService {
  private final BigDecimal baseFare;
  private final BigDecimal pricePerKm;
  private final BigDecimal minimumFare;

  public FareService(
      @Value("${fare.base-fare}") BigDecimal baseFare,
      @Value("${fare.price-per-km}") BigDecimal pricePerKm,
      @Value("${fare.minimum-fare}") BigDecimal minimumFare) {
    if (baseFare.signum() < 0 || pricePerKm.signum() < 0 || minimumFare.signum() <= 0)
      throw new IllegalArgumentException("Invalid fare configuration");
    this.baseFare = baseFare;
    this.pricePerKm = pricePerKm;
    this.minimumFare = minimumFare;
  }

  public BigDecimal calculate(double distanceKm) {
    if (!Double.isFinite(distanceKm) || distanceKm < 0)
      throw new IllegalArgumentException("Distance must be finite and non-negative");
    return baseFare
        .add(BigDecimal.valueOf(distanceKm).multiply(pricePerKm))
        .max(minimumFare)
        .setScale(2, RoundingMode.HALF_UP);
  }
}
