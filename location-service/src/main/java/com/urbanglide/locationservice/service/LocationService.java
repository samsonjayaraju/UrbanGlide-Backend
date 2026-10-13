package com.urbanglide.locationservice.service;

import com.urbanglide.locationservice.client.*;
import com.urbanglide.locationservice.dto.*;
import com.urbanglide.locationservice.entity.DriverLocation;
import com.urbanglide.locationservice.exception.*;
import com.urbanglide.locationservice.repository.DriverLocationRepository;
import com.urbanglide.locationservice.security.CurrentUser;
import java.time.Instant;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LocationService {
  private final DriverLocationRepository locations;
  private final DriverClient drivers;
  private final RideClient rides;
  private final long maxAge;

  public LocationService(
      DriverLocationRepository locations,
      DriverClient drivers,
      RideClient rides,
      @Value("${location.max-age-seconds:300}") long maxAge) {
    if (maxAge < 1) throw new IllegalArgumentException("Location max age must be positive");
    this.locations = locations;
    this.drivers = drivers;
    this.rides = rides;
    this.maxAge = maxAge;
  }

  @Transactional
  public LocationResponse updateMine(LocationRequest request) {
    CurrentUser.requireRole("DRIVER");
    return save(RemoteCalls.call(() -> drivers.byUser(CurrentUser.id())).driverId(), request);
  }

  @Transactional
  public LocationResponse updateInternal(Long id, LocationRequest request) {
    RemoteCalls.call(() -> drivers.byId(id));
    return save(id, request);
  }

  private LocationResponse save(Long id, LocationRequest r) {
    DriverLocation point =
        locations
            .findById(id)
            .orElseGet(
                () -> {
                  var p = new DriverLocation();
                  p.setDriverId(id);
                  return p;
                });
    point.setLatitude(r.latitude());
    point.setLongitude(r.longitude());
    point.setUpdatedAt(Instant.now());
    return response(locations.saveAndFlush(point));
  }

  public LocationResponse mine() {
    CurrentUser.requireRole("DRIVER");
    return get(RemoteCalls.call(() -> drivers.byUser(CurrentUser.id())).driverId());
  }

  public LocationResponse get(Long id) {
    return response(
        locations
            .findById(id)
            .orElseThrow(
                () -> new ResourceNotFoundException("Driver has not published a location")));
  }

  public LocationResponse forRide(Long id) {
    RideDetails ride = RemoteCalls.call(() -> rides.get(id));
    boolean owner =
        CurrentUser.role().equals("PASSENGER") && ride.passengerId().equals(CurrentUser.id());
    if (CurrentUser.role().equals("DRIVER"))
      owner =
          Objects.equals(
              ride.driverId(), RemoteCalls.call(() -> drivers.byUser(CurrentUser.id())).driverId());
    if (!owner)
      throw new UnauthorizedActionException(
          "Only this ride's passenger or assigned driver may track it");
    if (ride.driverId() == null
        || !Set.of("DRIVER_ASSIGNED", "ARRIVING", "ARRIVED", "IN_PROGRESS").contains(ride.status()))
      throw new ApiException(
          HttpStatus.CONFLICT, "Tracking is available only during an assigned active ride");
    return get(ride.driverId());
  }

  public List<NearbyLocationResponse> nearby(double lat, double lon, double radius) {
    return locations.findByUpdatedAtGreaterThanEqual(Instant.now().minusSeconds(maxAge)).stream()
        .map(
            p ->
                new NearbyLocationResponse(
                    p.getDriverId(),
                    GeoDistance.kilometers(lat, lon, p.getLatitude(), p.getLongitude())))
        .filter(p -> p.distanceKm() <= radius)
        .sorted(Comparator.comparingDouble(NearbyLocationResponse::distanceKm))
        .toList();
  }

  private LocationResponse response(DriverLocation p) {
    return new LocationResponse(
        p.getDriverId(),
        p.getLatitude(),
        p.getLongitude(),
        p.getUpdatedAt(),
        p.getUpdatedAt().isBefore(Instant.now().minusSeconds(maxAge)));
  }
}
