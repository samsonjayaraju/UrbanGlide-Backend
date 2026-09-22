package com.urbanglide.rideservice.repository;

import com.urbanglide.rideservice.entity.RideOffer;
import com.urbanglide.rideservice.enums.RideOfferStatus;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RideOfferRepository extends JpaRepository<RideOffer, Long> {
  List<RideOffer> findByRideId(Long rideId);

  Optional<RideOffer> findByRideIdAndDriverId(Long rideId, Long driverId);

  List<RideOffer> findByDriverIdAndStatusOrderByCreatedAtAsc(Long driverId, RideOfferStatus status);
}
