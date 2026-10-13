package com.urbanglide.paymentservice.repository;

import com.urbanglide.paymentservice.entity.NotificationOutbox;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationOutboxRepository extends JpaRepository<NotificationOutbox, Long> {
  boolean existsByEventKey(String key);

  List<NotificationOutbox> findTop20ByOrderByIdAsc();
}
