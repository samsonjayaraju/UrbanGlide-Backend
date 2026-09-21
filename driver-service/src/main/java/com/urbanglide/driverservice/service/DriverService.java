package com.urbanglide.driverservice.service;

import com.urbanglide.driverservice.dto.*;
import com.urbanglide.driverservice.entity.Driver;
import com.urbanglide.driverservice.enums.DriverAvailability;
import com.urbanglide.driverservice.exception.*;
import com.urbanglide.driverservice.repository.DriverRepository;
import com.urbanglide.driverservice.security.CurrentUser;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DriverService {
  private final DriverRepository drivers;

  public DriverService(DriverRepository drivers) {
    this.drivers = drivers;
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
    driver.setRating(0.0);
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

  @Transactional
  public DriverResponse location(DriverLocationRequest request) {
    CurrentUser.requireRole("DRIVER");
    Driver driver = locked(byUser(CurrentUser.id()).getDriverId());
    driver.setLatitude(request.latitude());
    driver.setLongitude(request.longitude());
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
    if (request.availability() == DriverAvailability.AVAILABLE && driver.getLatitude() == null)
      throw new ApiException(HttpStatus.BAD_REQUEST, "Set location before becoming AVAILABLE");
    driver.setAvailability(request.availability());
    return response(driver);
  }

  public List<NearbyDriverResponse> nearby(double lat, double lon, double radius) {
    return drivers.findByAvailability(DriverAvailability.AVAILABLE).stream()
        .filter(d -> d.getLatitude() != null && d.getLongitude() != null)
        .map(
            d ->
                new NearbyDriverResponse(
                    d.getDriverId(),
                    GeoDistance.kilometers(lat, lon, d.getLatitude(), d.getLongitude())))
        .filter(d -> d.distanceKm() <= radius)
        .sorted(Comparator.comparingDouble(NearbyDriverResponse::distanceKm))
        .toList();
  }

  public DriverResponse internalByUser(Long userId) {
    return response(byUser(userId));
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

  private DriverResponse response(Driver d) {
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
        d.getLatitude(),
        d.getLongitude(),
        d.getRating());
  }
}
