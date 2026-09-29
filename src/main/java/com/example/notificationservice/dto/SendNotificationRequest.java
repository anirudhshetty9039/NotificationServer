package com.example.notificationservice.dto;

import com.example.notificationservice.entity.NotificationMedium;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SendNotificationRequest(
        @NotNull(message = "userId is required") Long userId,
        @NotNull(message = "medium is required") NotificationMedium medium,
        @NotBlank(message = "message cannot be blank") String message
) {
}
