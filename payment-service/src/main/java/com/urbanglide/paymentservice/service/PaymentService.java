package com.urbanglide.paymentservice.service;

import com.urbanglide.paymentservice.dto.*;
import com.urbanglide.paymentservice.entity.Payment;
import com.urbanglide.paymentservice.enums.*;
import com.urbanglide.paymentservice.exception.*;
import com.urbanglide.paymentservice.repository.PaymentRepository;
import com.urbanglide.paymentservice.security.CurrentUser;
import java.time.Instant;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentService {
  private final PaymentRepository payments;

  public PaymentService(PaymentRepository payments) {
    this.payments = payments;
  }

  @Transactional
  public PaymentResponse create(CreatePaymentRequest request) {
    var existing = payments.findByRideId(request.rideId());
    if (existing.isPresent()) {
      Payment payment = existing.get();
      if (!payment.getPassengerId().equals(request.passengerId())
          || payment.getAmount().compareTo(request.amount()) != 0)
        throw new ApiException(HttpStatus.CONFLICT, "Payment for this ride has different details");
      return response(payment);
    }
    Payment payment = new Payment();
    payment.setRideId(request.rideId());
    payment.setPassengerId(request.passengerId());
    payment.setAmount(request.amount());
    payment.setPaymentMethod(PaymentMethod.CASH);
    payment.setPaymentStatus(PaymentStatus.PENDING);
    payment.setCreatedAt(Instant.now());
    return response(payments.saveAndFlush(payment));
  }

  public PaymentResponse byRide(Long rideId) {
    Payment p =
        payments
            .findByRideId(rideId)
            .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
    authorize(p);
    return response(p);
  }

  public PaymentResponse get(Long id) {
    Payment p = find(id);
    authorize(p);
    return response(p);
  }

  public List<PaymentResponse> my() {
    CurrentUser.requireRole("PASSENGER");
    return payments.findByPassengerIdOrderByCreatedAtDesc(CurrentUser.id()).stream()
        .map(this::response)
        .toList();
  }

  @Transactional
  public PaymentResponse complete(Long id, CompletePaymentRequest request) {
    Payment payment =
        payments
            .findLockedById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
    authorize(payment);
    if (payment.getPaymentStatus() == PaymentStatus.SUCCESS) {
      if (payment.getPaymentMethod() != request.paymentMethod())
        throw new ApiException(
            HttpStatus.CONFLICT, "Payment already completed using another method");
      return response(payment);
    }
    if (payment.getPaymentStatus() != PaymentStatus.PENDING)
      throw new ApiException(HttpStatus.CONFLICT, "Only PENDING payments can complete");
    payment.setPaymentMethod(request.paymentMethod());
    payment.setPaymentStatus(PaymentStatus.SUCCESS);
    payment.setTransactionReference("DEMO-" + UUID.randomUUID());
    return response(payment);
  }

  private Payment find(Long id) {
    return payments
        .findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
  }

  private void authorize(Payment p) {
    if (!CurrentUser.role().equals("PASSENGER") || !p.getPassengerId().equals(CurrentUser.id()))
      throw new UnauthorizedActionException("Payment belongs to another passenger");
  }

  private PaymentResponse response(Payment p) {
    return new PaymentResponse(
        p.getPaymentId(),
        p.getRideId(),
        p.getPassengerId(),
        p.getAmount(),
        p.getPaymentMethod(),
        p.getPaymentStatus(),
        p.getTransactionReference(),
        p.getCreatedAt());
  }
}
