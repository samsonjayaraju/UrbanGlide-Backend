package com.urbanglide.notificationservice.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "notifications")
public class Notification {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long notificationId;

  @Column(nullable = false, unique = true, length = 160)
  private String eventKey;

  @Column(nullable = false)
  private Long recipientId;

  private Long rideId;

  @Column(nullable = false, length = 40)
  private String type;

  @Column(nullable = false, length = 500)
  private String message;

  @Column(nullable = false)
  private Instant createdAt;

  private Instant readAt;

  public Notification() {}

  public Long getNotificationId() {
    return notificationId;
  }

  public void setNotificationId(Long value) {
    this.notificationId = value;
  }

  public String getEventKey() {
    return eventKey;
  }

  public void setEventKey(String value) {
    this.eventKey = value;
  }

  public Long getRecipientId() {
    return recipientId;
  }

  public void setRecipientId(Long value) {
    this.recipientId = value;
  }

  public Long getRideId() {
    return rideId;
  }

  public void setRideId(Long value) {
    this.rideId = value;
  }

  public String getType() {
    return type;
  }

  public void setType(String value) {
    this.type = value;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String value) {
    this.message = value;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant value) {
    this.createdAt = value;
  }

  public Instant getReadAt() {
    return readAt;
  }

  public void setReadAt(Instant value) {
    this.readAt = value;
  }
}
