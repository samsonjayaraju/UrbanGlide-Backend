package com.urbanglide.rideservice.service;

import com.urbanglide.rideservice.client.*;
import com.urbanglide.rideservice.dto.*;
import com.urbanglide.rideservice.entity.*;
import com.urbanglide.rideservice.enums.*;
import com.urbanglide.rideservice.exception.*;
import com.urbanglide.rideservice.repository.*;
import com.urbanglide.rideservice.security.CurrentUser;
import feign.FeignException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class RideService {
  private static final Logger log = LoggerFactory.getLogger(RideService.class);
  private final RideRepository rides;
  private final RideOfferRepository offers;
  private final DriverClient drivers;
  private final PaymentClient payments;
  private final FareService fares;
  private final TransactionTemplate transaction;
  private final double radius;

  public RideService(
      RideRepository rides,
      RideOfferRepository offers,
      DriverClient drivers,
      PaymentClient payments,
      FareService fares,
      PlatformTransactionManager manager,
      @Value("${matching.radius-km}") double radius) {
    this.rides = rides;
    this.offers = offers;
    this.drivers = drivers;
    this.payments = payments;
    this.fares = fares;
    this.transaction = new TransactionTemplate(manager);
    this.radius = radius;
    if (!Double.isFinite(radius) || radius <= 0 || radius > 100)
      throw new IllegalArgumentException("Matching radius must be between 0 and 100 km");
  }

  public RideResponseDto create(RideRequestDto request) {
    CurrentUser.requireRole("PASSENGER");
    return transaction.execute(
        tx -> {
          Ride ride = new Ride();
          ride.setPassengerId(CurrentUser.id());
          ride.setPickupLocation(request.pickupLocation());
          ride.setPickupLatitude(request.pickupLatitude());
          ride.setPickupLongitude(request.pickupLongitude());
          ride.setDropLocation(request.dropLocation());
          ride.setDropLatitude(request.dropLatitude());
          ride.setDropLongitude(request.dropLongitude());
          ride.setCreatedAt(Instant.now());
          ride.setCompletionSynced(false);
          ride.setStatus(RideStatus.REQUESTED);
          rides.saveAndFlush(ride);
          ride.setStatus(RideStatus.SEARCHING);
          var nearby =
              remote(
                  () ->
                      drivers.nearby(ride.getPickupLatitude(), ride.getPickupLongitude(), radius));
          for (NearbyDriverResponse driver : nearby) {
            RideOffer offer = new RideOffer();
            offer.setRideId(ride.getRideId());
            offer.setDriverId(driver.driverId());
            offer.setStatus(RideOfferStatus.PENDING);
            offer.setCreatedAt(Instant.now());
            offers.save(offer);
          }
          if (nearby.isEmpty()) ride.setStatus(RideStatus.NO_DRIVER_AVAILABLE);
          return response(ride);
        });
  }

  public List<RideOfferResponse> pendingOffers() {
    Long driverId = currentDriver();
    return offers
        .findByDriverIdAndStatusOrderByCreatedAtAsc(driverId, RideOfferStatus.PENDING)
        .stream()
        .map(
            o ->
                new RideOfferResponse(
                    o.getOfferId(),
                    o.getRideId(),
                    o.getDriverId(),
                    o.getStatus(),
                    o.getCreatedAt(),
                    response(find(o.getRideId()))))
        .toList();
  }

  public RideResponseDto accept(Long rideId) {
    Long driverId = currentDriver();
    AtomicBoolean reservationAttempted = new AtomicBoolean(false);
    try {
      return transaction.execute(
          tx -> {
            // This database row lock serializes acceptance across all ride-service instances.
            Ride ride = locked(rideId);
            if (ride.getStatus() != RideStatus.SEARCHING || ride.getDriverId() != null)
              throw new RideAlreadyAssignedException(
                  "Ride is already assigned or no longer searching");
            RideOffer offer = ownOffer(rideId, driverId);
            if (offer.getStatus() != RideOfferStatus.PENDING)
              throw new RideAlreadyAssignedException("Offer is no longer pending");
            reservationAttempted.set(true);
            remote(
                () -> {
                  drivers.reserve(driverId, new ReservationRequest(rideId));
                  return null;
                });
            ride.setDriverId(driverId);
            ride.setStatus(RideStatus.DRIVER_ASSIGNED);
            for (RideOffer other : offers.findByRideId(rideId)) {
              other.setStatus(
                  other.getDriverId().equals(driverId)
                      ? RideOfferStatus.ACCEPTED
                      : RideOfferStatus.CANCELLED);
            }
            rides.flush();
            offers.flush();
            return response(ride);
          });
    } catch (RuntimeException failure) {
      // REST calls are not part of the local database transaction. Undo a reservation on rollback.
      if (reservationAttempted.get()) {
        try {
          drivers.release(driverId, new ReservationRequest(rideId));
        } catch (RuntimeException releaseFailure) {
          log.error(
              "Reservation recovery needed: driver={}, ride={}", driverId, rideId, releaseFailure);
        }
      }
      throw failure;
    }
  }

  public RideResponseDto reject(Long rideId) {
    Long driverId = currentDriver();
    return transaction.execute(
        tx -> {
          Ride ride = locked(rideId);
          RideOffer offer = ownOffer(rideId, driverId);
          if (ride.getStatus() != RideStatus.SEARCHING
              || offer.getStatus() != RideOfferStatus.PENDING)
            throw new RideAlreadyAssignedException("Offer is no longer pending");
          offer.setStatus(RideOfferStatus.REJECTED);
          offers.flush();
          if (offers.findByRideId(rideId).stream()
              .noneMatch(o -> o.getStatus() == RideOfferStatus.PENDING))
            ride.setStatus(RideStatus.NO_DRIVER_AVAILABLE);
          return response(ride);
        });
  }

  public RideResponseDto updateStatus(Long rideId, RideStatus next) {
    Long driverId = currentDriver();
    RideResponseDto updated =
        transaction.execute(
            tx -> {
              Ride ride = locked(rideId);
              if (!driverId.equals(ride.getDriverId()))
                throw new UnauthorizedActionException(
                    "Only the assigned driver may update this ride");
              // A repeated COMPLETED request retries side effects, but never rewinds the lifecycle.
              if (ride.getStatus() == RideStatus.COMPLETED && next == RideStatus.COMPLETED)
                return response(ride);
              RideLifecycle.validateDriverTransition(ride.getStatus(), next);
              ride.setStatus(next);
              if (next == RideStatus.COMPLETED) {
                double distance =
                    GeoDistance.kilometers(
                        ride.getPickupLatitude(),
                        ride.getPickupLongitude(),
                        ride.getDropLatitude(),
                        ride.getDropLongitude());
                ride.setDistance(distance);
                ride.setFare(fares.calculate(distance));
                ride.setCompletedAt(Instant.now());
              }
              return response(ride);
            });
    if (next == RideStatus.COMPLETED) return synchronizeCompletion(rideId);
    return updated;
  }

  private RideResponseDto synchronizeCompletion(Long rideId) {
    // Completion is already durable. Idempotent remote calls can be repeated after a timeout.
    return transaction.execute(
        tx -> {
          Ride ride = locked(rideId);
          if (!ride.getCompletionSynced()) {
            remote(
                () -> {
                  payments.create(
                      new CreatePaymentRequest(rideId, ride.getPassengerId(), ride.getFare()));
                  return null;
                });
            remote(
                () -> {
                  drivers.release(ride.getDriverId(), new ReservationRequest(rideId));
                  return null;
                });
            ride.setCompletionSynced(true);
          }
          return response(ride);
        });
  }

  public RideResponseDto cancel(Long rideId) {
    CurrentUser.requireRole("PASSENGER");
    return transaction.execute(
        tx -> {
          Ride ride = locked(rideId);
          if (!ride.getPassengerId().equals(CurrentUser.id()))
            throw new UnauthorizedActionException("Ride belongs to another passenger");
          if (ride.getStatus() != RideStatus.SEARCHING && ride.getStatus() != RideStatus.REQUESTED)
            throw new InvalidRideStatusException("Cancellation is allowed only before assignment");
          ride.setStatus(RideStatus.CANCELLED);
          offers
              .findByRideId(rideId)
              .forEach(
                  o -> {
                    if (o.getStatus() == RideOfferStatus.PENDING)
                      o.setStatus(RideOfferStatus.CANCELLED);
                  });
          return response(ride);
        });
  }

  public List<RideResponseDto> my() {
    if (CurrentUser.role().equals("PASSENGER"))
      return rides.findByPassengerIdOrderByCreatedAtDesc(CurrentUser.id()).stream()
          .map(this::response)
          .toList();
    return rides.findByDriverIdOrderByCreatedAtDesc(currentDriver()).stream()
        .map(this::response)
        .toList();
  }

  public RideResponseDto get(Long rideId) {
    Ride ride = find(rideId);
    boolean owner =
        CurrentUser.role().equals("PASSENGER") && ride.getPassengerId().equals(CurrentUser.id());
    if (CurrentUser.role().equals("DRIVER")) owner = currentDriver().equals(ride.getDriverId());
    if (!owner) throw new UnauthorizedActionException("Ride does not belong to you");
    return response(ride);
  }

  private Long currentDriver() {
    CurrentUser.requireRole("DRIVER");
    return remote(() -> drivers.byUser(CurrentUser.id())).driverId();
  }

  private Ride find(Long id) {
    return rides.findById(id).orElseThrow(() -> new ResourceNotFoundException("Ride not found"));
  }

  private Ride locked(Long id) {
    return rides
        .findLockedById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Ride not found"));
  }

  private RideOffer ownOffer(Long rideId, Long driverId) {
    return offers
        .findByRideIdAndDriverId(rideId, driverId)
        .orElseThrow(() -> new UnauthorizedActionException("No offer for this driver"));
  }

  private <T> T remote(Supplier<T> action) {
    try {
      return action.get();
    } catch (FeignException ex) {
      if (ex.status() == 409)
        throw new ApiException(
            HttpStatus.CONFLICT, "Driver or payment state conflicts with this request");
      if (ex.status() == 404)
        throw new ResourceNotFoundException("Driver profile or remote record not found");
      throw new ApiException(
          HttpStatus.SERVICE_UNAVAILABLE,
          "A required service is unavailable. For completion failures, repeat COMPLETED to finish"
              + " synchronization.");
    }
  }

  private RideResponseDto response(Ride r) {
    return new RideResponseDto(
        r.getRideId(),
        r.getPassengerId(),
        r.getDriverId(),
        r.getPickupLocation(),
        r.getPickupLatitude(),
        r.getPickupLongitude(),
        r.getDropLocation(),
        r.getDropLatitude(),
        r.getDropLongitude(),
        r.getDistance(),
        r.getFare(),
        r.getStatus(),
        r.getCreatedAt(),
        r.getCompletedAt(),
        r.getCompletionSynced());
  }
}
