package com.urbanglide.locationservice.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "driver_locations")
public class DriverLocation {
  @Id private Long driverId;

  @Column(nullable = false)
  private Double latitude;

  @Column(nullable = false)
  private Double longitude;

  @Column(nullable = false)
  private Instant updatedAt;

  @Version private Long version;

  public DriverLocation() {}

  public Long getDriverId() {
    return driverId;
  }

  public void setDriverId(Long value) {
    this.driverId = value;
  }

  public Double getLatitude() {
    return latitude;
  }

  public void setLatitude(Double value) {
    this.latitude = value;
  }

  public Double getLongitude() {
    return longitude;
  }

  public void setLongitude(Double value) {
    this.longitude = value;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(Instant value) {
    this.updatedAt = value;
  }

  public Long getVersion() {
    return version;
  }

  public void setVersion(Long value) {
    this.version = value;
  }
}
