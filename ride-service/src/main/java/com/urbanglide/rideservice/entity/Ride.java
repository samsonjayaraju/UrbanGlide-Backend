package com.urbanglide.rideservice.entity;

import com.urbanglide.rideservice.enums.*;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "rides")
public class Ride {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long rideId;

  @Column(nullable = false)
  private Long passengerId;

  private Long driverId;

  @Column(nullable = false)
  private String pickupLocation;

  @Column(nullable = false)
  private Double pickupLatitude;

  @Column(nullable = false)
  private Double pickupLongitude;

  @Column(nullable = false)
  private String dropLocation;

  @Column(nullable = false)
  private Double dropLatitude;

  @Column(nullable = false)
  private Double dropLongitude;

  private Double distance;

  @Column(precision = 12, scale = 2)
  private BigDecimal fare;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private RideStatus status;

  @Column(nullable = false)
  private Instant createdAt;

  private Instant completedAt;

  @Column(nullable = false)
  private Boolean completionSynced;

  @Version private Long version;

  public Ride() {}

  public Long getRideId() {
    return rideId;
  }

  public void setRideId(Long rideId) {
    this.rideId = rideId;
  }

  public Long getPassengerId() {
    return passengerId;
  }

  public void setPassengerId(Long passengerId) {
    this.passengerId = passengerId;
  }

  public Long getDriverId() {
    return driverId;
  }

  public void setDriverId(Long driverId) {
    this.driverId = driverId;
  }

  public String getPickupLocation() {
    return pickupLocation;
  }

  public void setPickupLocation(String pickupLocation) {
    this.pickupLocation = pickupLocation;
  }

  public Double getPickupLatitude() {
    return pickupLatitude;
  }

  public void setPickupLatitude(Double pickupLatitude) {
    this.pickupLatitude = pickupLatitude;
  }

  public Double getPickupLongitude() {
    return pickupLongitude;
  }

  public void setPickupLongitude(Double pickupLongitude) {
    this.pickupLongitude = pickupLongitude;
  }

  public String getDropLocation() {
    return dropLocation;
  }

  public void setDropLocation(String dropLocation) {
    this.dropLocation = dropLocation;
  }

  public Double getDropLatitude() {
    return dropLatitude;
  }

  public void setDropLatitude(Double dropLatitude) {
    this.dropLatitude = dropLatitude;
  }

  public Double getDropLongitude() {
    return dropLongitude;
  }

  public void setDropLongitude(Double dropLongitude) {
    this.dropLongitude = dropLongitude;
  }

  public Double getDistance() {
    return distance;
  }

  public void setDistance(Double distance) {
    this.distance = distance;
  }

  public BigDecimal getFare() {
    return fare;
  }

  public void setFare(BigDecimal fare) {
    this.fare = fare;
  }

  public RideStatus getStatus() {
    return status;
  }

  public void setStatus(RideStatus status) {
    this.status = status;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }

  public Instant getCompletedAt() {
    return completedAt;
  }

  public void setCompletedAt(Instant completedAt) {
    this.completedAt = completedAt;
  }

  public Boolean getCompletionSynced() {
    return completionSynced;
  }

  public void setCompletionSynced(Boolean completionSynced) {
    this.completionSynced = completionSynced;
  }

  public Long getVersion() {
    return version;
  }

  public void setVersion(Long version) {
    this.version = version;
  }
}
