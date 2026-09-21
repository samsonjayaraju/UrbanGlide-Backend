package com.urbanglide.driverservice.entity;

import com.urbanglide.driverservice.enums.*;
import jakarta.persistence.*;

@Entity
@Table(name = "drivers")
public class Driver {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long driverId;

  @Column(nullable = false, unique = true)
  private Long userId;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private String phone;

  @Column(nullable = false, unique = true)
  private String licenseNumber;

  @Column(nullable = false, unique = true)
  private String vehicleNumber;

  @Column(nullable = false)
  private String vehicleModel;

  @Column(nullable = false)
  private String vehicleType;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private DriverAvailability availability;

  private Double latitude;

  private Double longitude;

  @Column(nullable = false)
  private Double rating;

  @Column(unique = true)
  private Long activeRideId;

  @Version private Long version;

  public Driver() {}

  public Long getDriverId() {
    return driverId;
  }

  public void setDriverId(Long driverId) {
    this.driverId = driverId;
  }

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public String getLicenseNumber() {
    return licenseNumber;
  }

  public void setLicenseNumber(String licenseNumber) {
    this.licenseNumber = licenseNumber;
  }

  public String getVehicleNumber() {
    return vehicleNumber;
  }

  public void setVehicleNumber(String vehicleNumber) {
    this.vehicleNumber = vehicleNumber;
  }

  public String getVehicleModel() {
    return vehicleModel;
  }

  public void setVehicleModel(String vehicleModel) {
    this.vehicleModel = vehicleModel;
  }

  public String getVehicleType() {
    return vehicleType;
  }

  public void setVehicleType(String vehicleType) {
    this.vehicleType = vehicleType;
  }

  public DriverAvailability getAvailability() {
    return availability;
  }

  public void setAvailability(DriverAvailability availability) {
    this.availability = availability;
  }

  public Double getLatitude() {
    return latitude;
  }

  public void setLatitude(Double latitude) {
    this.latitude = latitude;
  }

  public Double getLongitude() {
    return longitude;
  }

  public void setLongitude(Double longitude) {
    this.longitude = longitude;
  }

  public Double getRating() {
    return rating;
  }

  public void setRating(Double rating) {
    this.rating = rating;
  }

  public Long getActiveRideId() {
    return activeRideId;
  }

  public void setActiveRideId(Long activeRideId) {
    this.activeRideId = activeRideId;
  }

  public Long getVersion() {
    return version;
  }

  public void setVersion(Long version) {
    this.version = version;
  }
}
