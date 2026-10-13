package com.urbanglide.notificationservice.controller;

import com.urbanglide.notificationservice.dto.*;
import com.urbanglide.notificationservice.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/notifications")
public class InternalNotificationController {
  private final NotificationService service;

  public InternalNotificationController(NotificationService service) {
    this.service = service;
  }

  @PostMapping
  public NotificationResponse create(@Valid @RequestBody NotificationRequest request) {
    return service.create(request);
  }
}
