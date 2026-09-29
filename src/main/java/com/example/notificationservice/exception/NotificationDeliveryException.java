package com.example.notificationservice.exception;

public class NotificationDeliveryException extends NotificationException {
    public NotificationDeliveryException(String message) {
        super(message);
    }

    public NotificationDeliveryException(String message, Throwable cause) {
        super(message, cause);
    }
}
