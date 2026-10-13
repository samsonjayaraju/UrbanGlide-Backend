package com.urbanglide.ratingservice.repository;

import com.urbanglide.ratingservice.entity.Review;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface ReviewRepository extends JpaRepository<Review, Long> {
  boolean existsByRideId(Long rideId);

  List<Review> findByPassengerIdOrderByCreatedAtDesc(Long passengerId, Pageable page);

  List<Review> findByDriverIdOrderByCreatedAtDesc(Long driverId, Pageable page);

  long countByDriverId(Long driverId);

  @Query("select avg(r.rating) from Review r where r.driverId=:id")
  Double averageByDriverId(@Param("id") Long id);
}
