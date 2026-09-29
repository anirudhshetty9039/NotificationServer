package com.example.notificationservice;

import com.example.notificationservice.entity.User;
import com.example.notificationservice.exception.NotificationDeliveryException;
import com.example.notificationservice.exception.NotificationValidationException;
import com.example.notificationservice.provider.SmsProvider;
import com.example.notificationservice.strategy.SmsNotificationSender;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class SmsNotificationSenderTest {

    @Mock
    private SmsProvider smsProvider;

    @Test
    void shouldSendSuccess() {
        SmsNotificationSender sender = new SmsNotificationSender(smsProvider);
        User user = new User();
        user.setPhoneNumber("+1234567890");
        user.setNotificationsEnabled(true);

        sender.send(user, "OTP 123456");

        verify(smsProvider).send("+1234567890", "OTP 123456");
    }

    @Test
    void shouldFailWhenPhoneNumberMissing() {
        SmsNotificationSender sender = new SmsNotificationSender(smsProvider);
        User user = new User();
        user.setPhoneNumber(null);
        user.setNotificationsEnabled(true);

        assertThrows(NotificationValidationException.class, () -> sender.send(user, "OTP 123456"));
        verifyNoInteractions(smsProvider);
    }

    @Test
    void shouldFailWhenProviderThrows() {
        SmsNotificationSender sender = new SmsNotificationSender(smsProvider);
        User user = new User();
        user.setPhoneNumber("+1234567890");
        user.setNotificationsEnabled(true);
        doThrow(new NotificationDeliveryException("provider down")).when(smsProvider).send("+1234567890", "OTP 123456");

        assertThrows(NotificationDeliveryException.class, () -> sender.send(user, "OTP 123456"));
    }
}
