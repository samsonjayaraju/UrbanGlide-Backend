package com.urbanglide.driverservice.service;

public final class GeoDistance {
  private static final double EARTH_RADIUS_KM = 6371.0088;

  private GeoDistance() {}

  public static double kilometers(double lat1, double lon1, double lat2, double lon2) {
    double dLat = Math.toRadians(lat2 - lat1);
    double dLon = Math.toRadians(lon2 - lon1);
    double a =
        Math.pow(Math.sin(dLat / 2), 2)
            + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2))
                * Math.pow(Math.sin(dLon / 2), 2);
    // Clamp rounding errors before taking the square root and inverse sine.
    return 2 * EARTH_RADIUS_KM * Math.asin(Math.sqrt(Math.max(0, Math.min(1, a))));
  }
}
