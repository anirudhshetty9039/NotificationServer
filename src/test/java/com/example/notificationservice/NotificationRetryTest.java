package com.example.notificationservice;

import com.example.notificationservice.config.NotificationRetryProperties;
import com.example.notificationservice.entity.Notification;
import com.example.notificationservice.entity.NotificationMedium;
import com.example.notificationservice.entity.NotificationStatus;
import com.example.notificationservice.entity.User;
import com.example.notificationservice.exception.NotificationDeliveryException;
import com.example.notificationservice.repository.NotificationRepository;
import com.example.notificationservice.repository.UserRepository;
import com.example.notificationservice.service.NotificationService;
import com.example.notificationservice.strategy.NotificationSender;
import com.example.notificationservice.strategy.NotificationSenderRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationRetryTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationSenderRegistry senderRegistry;

    @Mock
    private NotificationSender sender;

    @Test
    void shouldRetryAndEventuallySucceed() {
        NotificationRetryProperties retryProperties = new NotificationRetryProperties();
        retryProperties.setMaxAttempts(3);
        retryProperties.setDelay(0);

        NotificationService service = new NotificationService(userRepository, notificationRepository, senderRegistry, retryProperties);
        User user = new User();
        user.setId(1L);
        user.setPhoneNumber("+111");
        user.setNotificationsEnabled(true);

        Notification notification = new Notification();
        notification.setId(10L);
        notification.setUserId(1L);
        notification.setMedium(NotificationMedium.SMS);
        notification.setMessage("Retry me");
        notification.setStatus(NotificationStatus.PENDING);

        when(senderRegistry.getSender(NotificationMedium.SMS)).thenReturn(sender);
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doThrow(new RuntimeException("temporary failure"))
                .doNothing()
                .when(sender).send(user, "Retry me");

        Notification processed = service.processNotification(notification, user);

        assertEquals(NotificationStatus.SENT, processed.getStatus());
        assertEquals(1, processed.getRetryCount());
    }

    @Test
    void shouldFailAfterMaxAttempts() {
        NotificationRetryProperties retryProperties = new NotificationRetryProperties();
        retryProperties.setMaxAttempts(2);
        retryProperties.setDelay(0);

        NotificationService service = new NotificationService(userRepository, notificationRepository, senderRegistry, retryProperties);
        User user = new User();
        user.setId(2L);
        user.setPhoneNumber("+222");
        user.setNotificationsEnabled(true);

        Notification notification = new Notification();
        notification.setId(11L);
        notification.setUserId(2L);
        notification.setMedium(NotificationMedium.SMS);
        notification.setMessage("Still failing");
        notification.setStatus(NotificationStatus.PENDING);

        when(senderRegistry.getSender(NotificationMedium.SMS)).thenReturn(sender);
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doThrow(new RuntimeException("temporary failure")).when(sender).send(user, "Still failing");

        Notification processed = service.processNotification(notification, user);

        assertEquals(NotificationStatus.FAILED, processed.getStatus());
    }
}
