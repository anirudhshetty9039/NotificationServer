package com.example.notificationservice.strategy;

import com.example.notificationservice.entity.NotificationMedium;
import com.example.notificationservice.entity.User;

public interface NotificationSender {
    NotificationMedium getMedium();

    void validate(User user);

    void send(User user, String message);
}
