package com.urbanglide.notificationservice.repository;

import com.urbanglide.notificationservice.entity.Notification;
import java.util.*;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
  Optional<Notification> findByEventKey(String key);

  List<Notification> findByRecipientIdOrderByNotificationIdDesc(Long id, Pageable page);

  List<Notification> findByRecipientIdAndReadAtIsNullOrderByNotificationIdDesc(
      Long id, Pageable page);

  long countByRecipientIdAndReadAtIsNull(Long id);
}
