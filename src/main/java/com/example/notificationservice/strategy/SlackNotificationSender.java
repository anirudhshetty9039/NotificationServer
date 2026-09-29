package com.example.notificationservice.strategy;

import com.example.notificationservice.entity.NotificationMedium;
import com.example.notificationservice.entity.User;
import com.example.notificationservice.exception.NotificationDeliveryException;
import com.example.notificationservice.provider.SlackProvider;
import org.springframework.stereotype.Component;

@Component
public class SlackNotificationSender extends AbstractNotificationSender {

    private final SlackProvider slackProvider;

    public SlackNotificationSender(SlackProvider slackProvider) {
        this.slackProvider = slackProvider;
    }

    @Override
    public NotificationMedium getMedium() {
        return NotificationMedium.SLACK;
    }

    @Override
    public void send(User user, String message) {
        validate(user);
        if (message == null || message.isBlank()) {
            throw new NotificationDeliveryException("Slack message cannot be blank");
        }
        slackProvider.send(user.getSlackId(), message);
    }
}
