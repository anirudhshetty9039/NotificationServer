package com.example.notificationservice.strategy;

import com.example.notificationservice.entity.NotificationMedium;
import com.example.notificationservice.entity.User;
import com.example.notificationservice.exception.NotificationDeliveryException;
import com.example.notificationservice.provider.SmsProvider;
import org.springframework.stereotype.Component;

@Component
public class SmsNotificationSender extends AbstractNotificationSender {

    private final SmsProvider smsProvider;

    public SmsNotificationSender(SmsProvider smsProvider) {
        this.smsProvider = smsProvider;
    }

    @Override
    public NotificationMedium getMedium() {
        return NotificationMedium.SMS;
    }

    @Override
    public void send(User user, String message) {
        validate(user);
        if (message == null || message.isBlank()) {
            throw new NotificationDeliveryException("SMS message cannot be blank");
        }
        smsProvider.send(user.getPhoneNumber(), message);
    }
}
