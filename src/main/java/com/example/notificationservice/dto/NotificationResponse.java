package com.example.notificationservice.dto;

import com.example.notificationservice.entity.NotificationMedium;
import com.example.notificationservice.entity.NotificationStatus;

import java.time.Instant;

public record NotificationResponse(
        Long id,
        Long userId,
        NotificationMedium medium,
        String message,
        NotificationStatus status,
        Instant scheduledAt,
        Instant createdAt,
        Instant updatedAt,
        int retryCount,
        String lastError
) {
}
