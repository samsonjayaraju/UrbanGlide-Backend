package com.urbanglide.rideservice.entity;

import com.urbanglide.rideservice.enums.*;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(
    name = "ride_offers",
    uniqueConstraints = @UniqueConstraint(columnNames = {"ride_id", "driver_id"}))
public class RideOffer {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long offerId;

  @Column(nullable = false)
  private Long rideId;

  @Column(nullable = false)
  private Long driverId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private RideOfferStatus status;

  @Column(nullable = false)
  private Instant createdAt;

  public RideOffer() {}

  public Long getOfferId() {
    return offerId;
  }

  public void setOfferId(Long offerId) {
    this.offerId = offerId;
  }

  public Long getRideId() {
    return rideId;
  }

  public void setRideId(Long rideId) {
    this.rideId = rideId;
  }

  public Long getDriverId() {
    return driverId;
  }

  public void setDriverId(Long driverId) {
    this.driverId = driverId;
  }

  public RideOfferStatus getStatus() {
    return status;
  }

  public void setStatus(RideOfferStatus status) {
    this.status = status;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }
}
