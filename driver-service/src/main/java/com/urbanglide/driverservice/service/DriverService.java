package com.urbanglide.driverservice.service;

import com.urbanglide.driverservice.client.*;
import com.urbanglide.driverservice.dto.*;
import com.urbanglide.driverservice.entity.Driver;
import com.urbanglide.driverservice.enums.DriverAvailability;
import com.urbanglide.driverservice.exception.*;
import com.urbanglide.driverservice.repository.DriverRepository;
import com.urbanglide.driverservice.security.CurrentUser;
import feign.FeignException;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DriverService {
  private final DriverRepository drivers;

  private final LocationClient locations;
  private final RatingClient ratings;

  public DriverService(DriverRepository drivers, LocationClient locations, RatingClient ratings) {
    this.drivers = drivers;
    this.locations = locations;
    this.ratings = ratings;
  }

  @Transactional
  public DriverResponse create(DriverProfileRequest request) {
    CurrentUser.requireRole("DRIVER");
    if (drivers.findByUserId(CurrentUser.id()).isPresent())
      throw new ApiException(HttpStatus.CONFLICT, "Driver profile already exists");
    Driver driver = new Driver();
    driver.setUserId(CurrentUser.id());
    apply(driver, request);
    driver.setAvailability(DriverAvailability.OFFLINE);
    driver.setLegacyRating(0.0);
    return response(drivers.saveAndFlush(driver));
  }

  public DriverResponse me() {
    CurrentUser.requireRole("DRIVER");
    return response(byUser(CurrentUser.id()));
  }

  public DriverResponse get(Long id) {
    Driver driver = find(id);
    if (!CurrentUser.role().equals("ADMIN") && !driver.getUserId().equals(CurrentUser.id()))
      throw new UnauthorizedActionException("Only your own profile is visible");
    return response(driver);
  }

  @Transactional
  public DriverResponse update(DriverProfileRequest request) {
    CurrentUser.requireRole("DRIVER");
    Driver driver = locked(byUser(CurrentUser.id()).getDriverId());
    apply(driver, request);
    return response(driver);
  }

  @Transactional
  public DriverResponse vehicle(VehicleRequest request) {
    CurrentUser.requireRole("DRIVER");
    Driver driver = locked(byUser(CurrentUser.id()).getDriverId());
    driver.setVehicleNumber(request.vehicleNumber());
    driver.setVehicleModel(request.vehicleModel());
    driver.setVehicleType(request.vehicleType());
    return response(driver);
  }

  public DriverResponse location(DriverLocationRequest request) {
    CurrentUser.requireRole("DRIVER");
    Driver driver = byUser(CurrentUser.id());
    RemoteCalls.call(() -> locations.update(driver.getDriverId(), request));
    return response(driver);
  }

  @Transactional
  public DriverResponse availability(DriverAvailabilityRequest request) {
    CurrentUser.requireRole("DRIVER");
    Driver driver = locked(byUser(CurrentUser.id()).getDriverId());
    if (driver.getActiveRideId() != null || request.availability() == DriverAvailability.BUSY)
      throw new ApiException(
          HttpStatus.CONFLICT,
          "BUSY is managed by ride assignment; an active ride must finish first");
    if (request.availability() == DriverAvailability.AVAILABLE) {
      LocationResponse point = RemoteCalls.call(() -> locations.get(driver.getDriverId()));
      if (point.stale())
        throw new ApiException(
            HttpStatus.BAD_REQUEST, "Update your location before becoming AVAILABLE");
    }
    driver.setAvailability(request.availability());
    return response(driver);
  }

  public List<NearbyDriverResponse> nearby(double lat, double lon, double radius) {
    var nearby = RemoteCalls.call(() -> locations.nearby(lat, lon, radius));
    var available = new HashSet<Long>();
    drivers.findAllById(nearby.stream().map(NearbyDriverResponse::driverId).toList()).stream()
        .filter(
            d -> d.getAvailability() == DriverAvailability.AVAILABLE && d.getActiveRideId() == null)
        .forEach(d -> available.add(d.getDriverId()));
    return nearby.stream().filter(d -> available.contains(d.driverId())).toList();
  }

  // Internal identity reads must not call Location/Rating again: that would create call cycles.
  public DriverResponse internalByUser(Long userId) {
    return identity(byUser(userId));
  }

  public DriverResponse internalById(Long id) {
    return identity(find(id));
  }

  @Transactional
  public void reserve(Long id, Long rideId) {
    Driver driver = locked(id);
    if (rideId.equals(driver.getActiveRideId())) return; // Retry of the same assignment is safe.
    if (driver.getAvailability() != DriverAvailability.AVAILABLE
        || driver.getActiveRideId() != null)
      throw new ApiException(HttpStatus.CONFLICT, "Driver is no longer available");
    driver.setActiveRideId(rideId);
    driver.setAvailability(DriverAvailability.BUSY);
  }

  @Transactional
  public void release(Long id, Long rideId) {
    Driver driver = locked(id);
    if (driver.getActiveRideId() == null) return;
    // A delayed retry for an old ride must not release a newer reservation.
    if (!rideId.equals(driver.getActiveRideId())) return;
    driver.setActiveRideId(null);
    driver.setAvailability(DriverAvailability.AVAILABLE);
  }

  private Driver find(Long id) {
    return drivers
        .findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Driver not found"));
  }

  private Driver locked(Long id) {
    return drivers
        .findLockedById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Driver not found"));
  }

  private Driver byUser(Long id) {
    return drivers
        .findByUserId(id)
        .orElseThrow(() -> new ResourceNotFoundException("Create a driver profile first"));
  }

  private void apply(Driver d, DriverProfileRequest r) {
    d.setName(r.name());
    d.setPhone(r.phone());
    d.setLicenseNumber(r.licenseNumber());
    d.setVehicleNumber(r.vehicleNumber());
    d.setVehicleModel(r.vehicleModel());
    d.setVehicleType(r.vehicleType());
  }

  private DriverResponse identity(Driver d) {
    return new DriverResponse(
        d.getDriverId(),
        d.getUserId(),
        d.getName(),
        d.getPhone(),
        d.getLicenseNumber(),
        d.getVehicleNumber(),
        d.getVehicleModel(),
        d.getVehicleType(),
        d.getAvailability(),
        null,
        null,
        null);
  }

  private DriverResponse response(Driver d) {
    LocationResponse point = null;
    Double rating = null;
    // A profile remains usable when an optional summary service is down. Null means unavailable.
    try {
      point = locations.get(d.getDriverId());
    } catch (FeignException ignored) {
    }
    try {
      rating = ratings.summary(d.getDriverId()).averageRating();
    } catch (FeignException ignored) {
    }
    return new DriverResponse(
        d.getDriverId(),
        d.getUserId(),
        d.getName(),
        d.getPhone(),
        d.getLicenseNumber(),
        d.getVehicleNumber(),
        d.getVehicleModel(),
        d.getVehicleType(),
        d.getAvailability(),
        point == null ? null : point.latitude(),
        point == null ? null : point.longitude(),
        rating);
  }
}
