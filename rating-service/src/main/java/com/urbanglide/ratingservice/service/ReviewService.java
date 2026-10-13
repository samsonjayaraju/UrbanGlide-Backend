package com.urbanglide.ratingservice.service;

import com.urbanglide.ratingservice.client.*;
import com.urbanglide.ratingservice.dto.*;
import com.urbanglide.ratingservice.entity.Review;
import com.urbanglide.ratingservice.exception.*;
import com.urbanglide.ratingservice.repository.ReviewRepository;
import com.urbanglide.ratingservice.security.CurrentUser;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReviewService {
  private final ReviewRepository reviews;
  private final RideClient rides;
  private final DriverClient drivers;

  public ReviewService(ReviewRepository reviews, RideClient rides, DriverClient drivers) {
    this.reviews = reviews;
    this.rides = rides;
    this.drivers = drivers;
  }

  @Transactional
  public ReviewResponse create(ReviewRequest request) {
    CurrentUser.requireRole("PASSENGER");
    RideDetails ride = RemoteCalls.call(() -> rides.get(request.rideId()));
    if (!ride.passengerId().equals(CurrentUser.id()))
      throw new UnauthorizedActionException("Only the passenger who took this ride can review it");
    if (!"COMPLETED".equals(ride.status()) || ride.driverId() == null)
      throw new ApiException(HttpStatus.CONFLICT, "Only completed rides can be reviewed");
    if (reviews.existsByRideId(ride.rideId()))
      throw new ApiException(HttpStatus.CONFLICT, "This ride already has a review");
    Review r = new Review();
    r.setRideId(ride.rideId());
    r.setPassengerId(ride.passengerId());
    r.setDriverId(ride.driverId());
    r.setRating(request.rating());
    r.setComment(request.comment().trim());
    r.setCreatedAt(Instant.now());
    return response(reviews.saveAndFlush(r));
  }

  public List<ReviewResponse> my(int page, int size) {
    var paging = PageRequest.of(page, size);
    if (CurrentUser.role().equals("PASSENGER"))
      return reviews.findByPassengerIdOrderByCreatedAtDesc(CurrentUser.id(), paging).stream()
          .map(this::response)
          .toList();
    CurrentUser.requireRole("DRIVER");
    Long id = RemoteCalls.call(() -> drivers.byUser(CurrentUser.id())).driverId();
    return reviews.findByDriverIdOrderByCreatedAtDesc(id, paging).stream()
        .map(this::response)
        .toList();
  }

  @Transactional(readOnly = true)
  public RatingSummary summary(Long id) {
    RemoteCalls.call(() -> drivers.byId(id));
    return internalSummary(id);
  }

  @Transactional(readOnly = true)
  public RatingSummary internalSummary(Long id) {
    Double avg = reviews.averageByDriverId(id);
    return new RatingSummary(
        id,
        reviews.countByDriverId(id),
        avg == null
            ? 0.0
            : BigDecimal.valueOf(avg).setScale(2, RoundingMode.HALF_UP).doubleValue());
  }

  private ReviewResponse response(Review r) {
    return new ReviewResponse(
        r.getReviewId(),
        r.getRideId(),
        r.getDriverId(),
        r.getRating(),
        r.getComment(),
        r.getCreatedAt());
  }
}
