package com.example.notificationservice.strategy;

import com.example.notificationservice.entity.NotificationMedium;
import com.example.notificationservice.entity.User;
import com.example.notificationservice.exception.NotificationValidationException;

public abstract class AbstractNotificationSender implements NotificationSender {

    @Override
    public void validate(User user) {
        if (user == null) {
            throw new NotificationValidationException("User is required");
        }
        if (!user.isNotificationsEnabled()) {
            throw new NotificationValidationException("User is unsubscribed from notifications");
        }
        if (!user.hasContactInfo(getMedium())) {
            throw new NotificationValidationException(getMedium() + " requires a valid contact for this user");
        }
    }

    protected String contactLabel(NotificationMedium medium) {
        return switch (medium) {
            case SMS -> "phone number";
            case WHATSAPP -> "WhatsApp number";
            case SLACK -> "Slack ID";
            case EMAIL -> "email address";
        };
    }
}
