package com.urbanglide.rideservice.repository;

import com.urbanglide.rideservice.entity.Ride;
import jakarta.persistence.LockModeType;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface RideRepository extends JpaRepository<Ride, Long> {
  List<Ride> findByPassengerIdOrderByCreatedAtDesc(Long passengerId);

  List<Ride> findByDriverIdOrderByCreatedAtDesc(Long driverId);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select r from Ride r where r.rideId = :id")
  Optional<Ride> findLockedById(@Param("id") Long id);
}
