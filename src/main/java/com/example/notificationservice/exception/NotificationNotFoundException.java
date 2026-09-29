package com.example.notificationservice.exception;

public class NotificationNotFoundException extends NotificationException {
    public NotificationNotFoundException(Long notificationId) {
        super("Notification with id " + notificationId + " was not found");
    }
}
