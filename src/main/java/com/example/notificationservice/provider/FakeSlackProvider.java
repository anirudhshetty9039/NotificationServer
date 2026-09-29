package com.example.notificationservice.provider;

import com.example.notificationservice.exception.NotificationDeliveryException;
import org.springframework.stereotype.Component;

@Component
public class FakeSlackProvider implements SlackProvider {

    @Override
    public void send(String slackId, String message) {
        if (slackId == null || slackId.isBlank()) {
            throw new NotificationDeliveryException("Slack delivery requires a Slack ID");
        }
        if (message == null || message.isBlank()) {
            throw new NotificationDeliveryException("Slack delivery requires a message");
        }
        // Fake Slack integration point.
    }
}
