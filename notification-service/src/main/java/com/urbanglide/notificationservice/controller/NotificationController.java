package com.urbanglide.notificationservice.controller;

import com.urbanglide.notificationservice.dto.*;
import com.urbanglide.notificationservice.service.NotificationService;
import jakarta.validation.constraints.*;
import java.util.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequestMapping("/api/notifications")
public class NotificationController {
  private final NotificationService service;

  public NotificationController(NotificationService service) {
    this.service = service;
  }

  @GetMapping("/my")
  public List<NotificationResponse> my(
      @RequestParam(defaultValue = "false") boolean unreadOnly,
      @RequestParam(defaultValue = "0") @Min(0) int page,
      @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
    return service.my(unreadOnly, page, size);
  }

  @GetMapping("/unread-count")
  public Map<String, Long> count() {
    return service.unreadCount();
  }

  @PatchMapping("/{id}/read")
  public NotificationResponse read(@PathVariable @Positive Long id) {
    return service.markRead(id);
  }
}
