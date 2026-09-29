package com.example.notificationservice.controller;

import com.example.notificationservice.dto.NotificationResponse;
import com.example.notificationservice.dto.ScheduleNotificationRequest;
import com.example.notificationservice.dto.SendNotificationRequest;
import com.example.notificationservice.entity.Notification;
import com.example.notificationservice.entity.NotificationStatus;
import com.example.notificationservice.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping("/send")
    public ResponseEntity<NotificationResponse> sendNotification(@Valid @RequestBody SendNotificationRequest request) {
        Notification notification = notificationService.sendImmediateNotification(request.userId(), request.medium(), request.message());
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(notification));
    }

    @PostMapping("/schedule")
    public ResponseEntity<NotificationResponse> scheduleNotification(@Valid @RequestBody ScheduleNotificationRequest request) {
        Notification notification = notificationService.scheduleNotification(
                request.userId(),
                request.medium(),
                request.message(),
                request.scheduledAt()
        );
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(toResponse(notification));
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotificationResponse> getNotification(@PathVariable Long id) {
        Notification notification = notificationService.getNotificationById(id);
        return ResponseEntity.ok(toResponse(notification));
    }

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getNotifications(@RequestParam(required = false) NotificationStatus status) {
        List<Notification> notifications = status == null
                ? notificationService.getNotificationsByStatus(NotificationStatus.PENDING)
                : notificationService.getNotificationsByStatus(status);
        return ResponseEntity.ok(notifications.stream().map(this::toResponse).toList());
    }

    private NotificationResponse toResponse(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getUserId(),
                notification.getMedium(),
                notification.getMessage(),
                notification.getStatus(),
                notification.getScheduledAt(),
                notification.getCreatedAt(),
                notification.getUpdatedAt(),
                notification.getRetryCount(),
                notification.getLastError()
        );
    }
}
