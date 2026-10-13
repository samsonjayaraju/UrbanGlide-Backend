package com.urbanglide.rideservice.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "notification_outbox")
public class NotificationOutbox {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 160)
  private String eventKey;

  @Column(nullable = false)
  private Long recipientId;

  @Column(nullable = false)
  private Long rideId;

  @Column(nullable = false, length = 40)
  private String type;

  @Column(nullable = false, length = 500)
  private String message;

  public NotificationOutbox() {}

  public Long getId() {
    return id;
  }

  public void setId(Long value) {
    this.id = value;
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
}
