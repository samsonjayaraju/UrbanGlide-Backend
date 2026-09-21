package com.urbanglide.driverservice.repository;

import com.urbanglide.driverservice.entity.Driver;
import com.urbanglide.driverservice.enums.DriverAvailability;
import jakarta.persistence.LockModeType;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface DriverRepository extends JpaRepository<Driver, Long> {
  Optional<Driver> findByUserId(Long userId);

  List<Driver> findByAvailability(DriverAvailability availability);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select d from Driver d where d.driverId = :id")
  Optional<Driver> findLockedById(@Param("id") Long id);
}
