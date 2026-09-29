package com.example.notificationservice.strategy;

import com.example.notificationservice.entity.NotificationMedium;
import com.example.notificationservice.exception.UnsupportedNotificationMediumException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class NotificationSenderRegistry {

    private final Map<NotificationMedium, NotificationSender> senders;

    public NotificationSenderRegistry(List<NotificationSender> senderList) {
        this.senders = senderList.stream()
                .collect(Collectors.toMap(NotificationSender::getMedium, Function.identity()));
    }

    public NotificationSender getSender(NotificationMedium medium) {
        if (medium == null) {
            throw new UnsupportedNotificationMediumException("Notification medium cannot be null");
        }
        NotificationSender sender = senders.get(medium);
        if (sender == null) {
            throw new UnsupportedNotificationMediumException("Unsupported notification medium: " + medium);
        }
        return sender;
    }
}
