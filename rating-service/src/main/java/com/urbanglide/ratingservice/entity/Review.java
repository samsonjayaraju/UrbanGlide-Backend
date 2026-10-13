package com.urbanglide.ratingservice.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "reviews")
public class Review {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long reviewId;

  @Column(nullable = false, unique = true)
  private Long rideId;

  @Column(nullable = false)
  private Long passengerId;

  @Column(nullable = false)
  private Long driverId;

  @Column(nullable = false)
  private Integer rating;

  @Column(nullable = false, length = 1000)
  private String comment;

  @Column(nullable = false)
  private Instant createdAt;

  public Review() {}

  public Long getReviewId() {
    return reviewId;
  }

  public void setReviewId(Long value) {
    this.reviewId = value;
  }

  public Long getRideId() {
    return rideId;
  }

  public void setRideId(Long value) {
    this.rideId = value;
  }

  public Long getPassengerId() {
    return passengerId;
  }

  public void setPassengerId(Long value) {
    this.passengerId = value;
  }

  public Long getDriverId() {
    return driverId;
  }

  public void setDriverId(Long value) {
    this.driverId = value;
  }

  public Integer getRating() {
    return rating;
  }

  public void setRating(Integer value) {
    this.rating = value;
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String value) {
    this.comment = value;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant value) {
    this.createdAt = value;
  }
}
