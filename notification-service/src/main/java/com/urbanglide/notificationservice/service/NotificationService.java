package com.urbanglide.notificationservice.service;

import com.urbanglide.notificationservice.dto.*;
import com.urbanglide.notificationservice.entity.Notification;
import com.urbanglide.notificationservice.exception.*;
import com.urbanglide.notificationservice.repository.NotificationRepository;
import com.urbanglide.notificationservice.security.CurrentUser;
import java.time.Instant;
import java.util.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {
  private final NotificationRepository notifications;

  public NotificationService(NotificationRepository notifications) {
    this.notifications = notifications;
  }

  @Transactional
  public NotificationResponse create(NotificationRequest r) {
    var existing = notifications.findByEventKey(r.eventKey());
    if (existing.isPresent()) {
      Notification n = existing.get();
      if (!n.getRecipientId().equals(r.recipientId())
          || !Objects.equals(n.getRideId(), r.rideId())
          || !n.getType().equals(r.type())
          || !n.getMessage().equals(r.message()))
        throw new ApiException(
            HttpStatus.CONFLICT, "Event key already belongs to a different notification");
      return response(n);
    }
    Notification n = new Notification();
    n.setEventKey(r.eventKey());
    n.setRecipientId(r.recipientId());
    n.setRideId(r.rideId());
    n.setType(r.type());
    n.setMessage(r.message());
    n.setCreatedAt(Instant.now());
    return response(notifications.saveAndFlush(n));
  }

  public List<NotificationResponse> my(boolean unreadOnly, int page, int size) {
    var paging = PageRequest.of(page, size);
    var found =
        unreadOnly
            ? notifications.findByRecipientIdAndReadAtIsNullOrderByNotificationIdDesc(
                CurrentUser.id(), paging)
            : notifications.findByRecipientIdOrderByNotificationIdDesc(CurrentUser.id(), paging);
    return found.stream().map(this::response).toList();
  }

  public Map<String, Long> unreadCount() {
    return Map.of("unreadCount", notifications.countByRecipientIdAndReadAtIsNull(CurrentUser.id()));
  }

  @Transactional
  public NotificationResponse markRead(Long id) {
    Notification n =
        notifications
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
    if (!n.getRecipientId().equals(CurrentUser.id()))
      throw new UnauthorizedActionException("Notification belongs to another user");
    if (n.getReadAt() == null) n.setReadAt(Instant.now());
    return response(n);
  }

  private NotificationResponse response(Notification n) {
    return new NotificationResponse(
        n.getNotificationId(),
        n.getRideId(),
        n.getType(),
        n.getMessage(),
        n.getCreatedAt(),
        n.getReadAt());
  }
}
