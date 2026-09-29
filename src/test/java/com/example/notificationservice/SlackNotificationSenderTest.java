package com.example.notificationservice;

import com.example.notificationservice.entity.User;
import com.example.notificationservice.exception.NotificationDeliveryException;
import com.example.notificationservice.exception.NotificationValidationException;
import com.example.notificationservice.provider.SlackProvider;
import com.example.notificationservice.strategy.SlackNotificationSender;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class SlackNotificationSenderTest {

    @Mock
    private SlackProvider slackProvider;

    @Test
    void shouldSendSuccess() {
        SlackNotificationSender sender = new SlackNotificationSender(slackProvider);
        User user = new User();
        user.setSlackId("@alice");
        user.setNotificationsEnabled(true);

        sender.send(user, "Daily summary");

        verify(slackProvider).send("@alice", "Daily summary");
    }

    @Test
    void shouldFailWhenSlackIdMissing() {
        SlackNotificationSender sender = new SlackNotificationSender(slackProvider);
        User user = new User();
        user.setSlackId(null);
        user.setNotificationsEnabled(true);

        assertThrows(NotificationValidationException.class, () -> sender.send(user, "Daily summary"));
        verifyNoInteractions(slackProvider);
    }

    @Test
    void shouldFailWhenProviderThrows() {
        SlackNotificationSender sender = new SlackNotificationSender(slackProvider);
        User user = new User();
        user.setSlackId("@alice");
        user.setNotificationsEnabled(true);
        doThrow(new NotificationDeliveryException("provider down")).when(slackProvider).send("@alice", "Daily summary");

        assertThrows(NotificationDeliveryException.class, () -> sender.send(user, "Daily summary"));
    }
}
