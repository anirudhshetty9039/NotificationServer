package com.example.notificationservice.dto;

import com.example.notificationservice.entity.NotificationMedium;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record ScheduleNotificationRequest(
        @NotNull(message = "userId is required") Long userId,
        @NotNull(message = "medium is required") NotificationMedium medium,
        @NotBlank(message = "message cannot be blank") String message,
        @NotNull(message = "scheduledAt is required")
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        Instant scheduledAt
) {
}
