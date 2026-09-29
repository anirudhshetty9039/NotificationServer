package com.example.notificationservice.exception;

public class UserNotFoundException extends NotificationException {
    public UserNotFoundException(Long userId) {
        super("User with id " + userId + " was not found");
    }
}
