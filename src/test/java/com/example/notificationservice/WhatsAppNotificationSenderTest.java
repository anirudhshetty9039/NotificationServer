package com.example.notificationservice;

import com.example.notificationservice.entity.User;
import com.example.notificationservice.exception.NotificationDeliveryException;
import com.example.notificationservice.exception.NotificationValidationException;
import com.example.notificationservice.provider.WhatsAppProvider;
import com.example.notificationservice.strategy.WhatsAppNotificationSender;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class WhatsAppNotificationSenderTest {

    @Mock
    private WhatsAppProvider whatsAppProvider;

    @Test
    void shouldSendSuccess() {
        WhatsAppNotificationSender sender = new WhatsAppNotificationSender(whatsAppProvider);
        User user = new User();
        user.setWhatsappNumber("+9876543210");
        user.setNotificationsEnabled(true);

        sender.send(user, "OTP 654321");

        verify(whatsAppProvider).send("+9876543210", "OTP 654321");
    }

    @Test
    void shouldFailWhenWhatsAppNumberMissing() {
        WhatsAppNotificationSender sender = new WhatsAppNotificationSender(whatsAppProvider);
        User user = new User();
        user.setWhatsappNumber(null);
        user.setNotificationsEnabled(true);

        assertThrows(NotificationValidationException.class, () -> sender.send(user, "OTP 654321"));
        verifyNoInteractions(whatsAppProvider);
    }

    @Test
    void shouldFailWhenProviderThrows() {
        WhatsAppNotificationSender sender = new WhatsAppNotificationSender(whatsAppProvider);
        User user = new User();
        user.setWhatsappNumber("+9876543210");
        user.setNotificationsEnabled(true);
        doThrow(new NotificationDeliveryException("provider down")).when(whatsAppProvider).send("+9876543210", "OTP 654321");

        assertThrows(NotificationDeliveryException.class, () -> sender.send(user, "OTP 654321"));
    }
}
