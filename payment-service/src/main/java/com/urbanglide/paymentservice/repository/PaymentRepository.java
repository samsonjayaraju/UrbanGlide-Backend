package com.urbanglide.paymentservice.repository;

import com.urbanglide.paymentservice.entity.Payment;
import jakarta.persistence.LockModeType;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
  Optional<Payment> findByRideId(Long rideId);

  List<Payment> findByPassengerIdOrderByCreatedAtDesc(Long passengerId);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select p from Payment p where p.paymentId = :id")
  Optional<Payment> findLockedById(@Param("id") Long id);
}
