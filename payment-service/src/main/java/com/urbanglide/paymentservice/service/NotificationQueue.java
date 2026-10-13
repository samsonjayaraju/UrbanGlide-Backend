package com.urbanglide.paymentservice.service;

import com.urbanglide.paymentservice.entity.NotificationOutbox;
import com.urbanglide.paymentservice.repository.NotificationOutboxRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;

@Service
public class NotificationQueue {
  private final NotificationOutboxRepository outbox;

  public NotificationQueue(NotificationOutboxRepository outbox) {
    this.outbox = outbox;
  }

  // The caller's transaction owns this row: rollback means no false notification is delivered.
  @Transactional(propagation = Propagation.MANDATORY)
  public void add(String key, Long recipientId, Long rideId, String type, String message) {
    if (outbox.existsByEventKey(key)) return;
    var item = new NotificationOutbox();
    item.setEventKey(key);
    item.setRecipientId(recipientId);
    item.setRideId(rideId);
    item.setType(type);
    item.setMessage(message);
    outbox.save(item);
  }
}
