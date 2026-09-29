package com.example.notificationservice.strategy;

import com.example.notificationservice.entity.NotificationMedium;
import com.example.notificationservice.entity.User;
import com.example.notificationservice.exception.NotificationDeliveryException;
import com.example.notificationservice.provider.WhatsAppProvider;
import org.springframework.stereotype.Component;

@Component
public class WhatsAppNotificationSender extends AbstractNotificationSender {

    private final WhatsAppProvider whatsAppProvider;

    public WhatsAppNotificationSender(WhatsAppProvider whatsAppProvider) {
        this.whatsAppProvider = whatsAppProvider;
    }

    @Override
    public NotificationMedium getMedium() {
        return NotificationMedium.WHATSAPP;
    }

    @Override
    public void send(User user, String message) {
        validate(user);
        if (message == null || message.isBlank()) {
            throw new NotificationDeliveryException("WhatsApp message cannot be blank");
        }
        whatsAppProvider.send(user.getWhatsappNumber(), message);
    }
}
