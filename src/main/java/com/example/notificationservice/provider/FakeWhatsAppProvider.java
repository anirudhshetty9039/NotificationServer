package com.example.notificationservice.provider;

import com.example.notificationservice.exception.NotificationDeliveryException;
import org.springframework.stereotype.Component;

@Component
public class FakeWhatsAppProvider implements WhatsAppProvider {

    @Override
    public void send(String whatsappNumber, String message) {
        if (whatsappNumber == null || whatsappNumber.isBlank()) {
            throw new NotificationDeliveryException("WhatsApp delivery requires a WhatsApp number");
        }
        if (message == null || message.isBlank()) {
            throw new NotificationDeliveryException("WhatsApp delivery requires a message");
        }
        // Fake WhatsApp integration point.
    }
}
