package com.urbanglide.locationservice.repository;

import com.urbanglide.locationservice.entity.DriverLocation;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DriverLocationRepository extends JpaRepository<DriverLocation, Long> {
  List<DriverLocation> findByUpdatedAtGreaterThanEqual(Instant cutoff);
}
