package com.example.notificationservice.provider;

import com.example.notificationservice.exception.NotificationDeliveryException;
import org.springframework.stereotype.Component;

@Component
public class FakeSmsProvider implements SmsProvider {

    @Override
    public void send(String phoneNumber, String message) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw new NotificationDeliveryException("SMS delivery requires a phone number");
        }
        if (message == null || message.isBlank()) {
            throw new NotificationDeliveryException("SMS delivery requires a message");
        }
        // Fake SMS gateway integration point.
    }
}
