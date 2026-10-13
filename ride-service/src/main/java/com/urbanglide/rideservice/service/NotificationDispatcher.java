package com.urbanglide.rideservice.service;

import com.urbanglide.rideservice.client.NotificationClient;
import com.urbanglide.rideservice.dto.NotificationRequest;
import com.urbanglide.rideservice.repository.NotificationOutboxRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
    name = "notifications.delivery.enabled",
    havingValue = "true",
    matchIfMissing = true)
public class NotificationDispatcher {
  private static final Logger log = LoggerFactory.getLogger(NotificationDispatcher.class);
  private final NotificationOutboxRepository outbox;
  private final NotificationClient client;

  public NotificationDispatcher(NotificationOutboxRepository outbox, NotificationClient client) {
    this.outbox = outbox;
    this.client = client;
  }

  @Scheduled(fixedDelayString = "${notifications.delivery.delay-ms:5000}")
  public void deliverPending() {
    for (var n : outbox.findTop20ByOrderByIdAsc()) {
      try {
        client.create(
            new NotificationRequest(
                n.getEventKey(), n.getRecipientId(), n.getRideId(), n.getType(), n.getMessage()));
        outbox.deleteById(n.getId());
      } catch (RuntimeException failure) {
        log.warn(
            "Notification {} remains queued: {}",
            n.getEventKey(),
            failure.getClass().getSimpleName());
      }
    }
  }
}
