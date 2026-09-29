package com.example.notificationservice;

import com.example.notificationservice.entity.Notification;
import com.example.notificationservice.entity.NotificationMedium;
import com.example.notificationservice.entity.NotificationStatus;
import com.example.notificationservice.entity.User;
import com.example.notificationservice.exception.NotificationDeliveryException;
import com.example.notificationservice.exception.UserNotFoundException;
import com.example.notificationservice.repository.NotificationRepository;
import com.example.notificationservice.repository.UserRepository;
import com.example.notificationservice.service.NotificationService;
import com.example.notificationservice.strategy.NotificationSender;
import com.example.notificationservice.strategy.NotificationSenderRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class NotificationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationSenderRegistry senderRegistry;

    @Mock
    private NotificationSender smsSender;

    @InjectMocks
    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(senderRegistry.getSender(NotificationMedium.SMS)).thenReturn(smsSender);
    }

    @Test
    void shouldSendNotificationUsingCorrectSender() {
        User user = new User();
        user.setId(1L);
        user.setPhoneNumber("+1234567890");
        user.setName("Alice");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(smsSender).send(user, "OTP 123456");

        Notification notification = notificationService.sendImmediateNotification(1L, NotificationMedium.SMS, "OTP 123456");

        assertEquals(NotificationStatus.SENT, notification.getStatus());
        verify(smsSender).send(user, "OTP 123456");
    }

    @Test
    void shouldThrowWhenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> notificationService.sendImmediateNotification(99L, NotificationMedium.SMS, "hello"));
    }

    @Test
    void shouldFailWhenSenderThrowsException() {
        User user = new User();
        user.setId(2L);
        user.setPhoneNumber("+111");
        user.setName("Bob");

        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doThrow(new NotificationDeliveryException("Provider unavailable")).when(smsSender).send(user, "hello");

        Notification notification = notificationService.sendImmediateNotification(2L, NotificationMedium.SMS, "hello");

        assertEquals(NotificationStatus.FAILED, notification.getStatus());
        assertNotNull(notification.getLastError());
    }

    @Test
    void shouldSetScheduledNotificationToPending() {
        User user = new User();
        user.setId(3L);
        user.setPhoneNumber("+222");
        user.setName("Carol");

        when(userRepository.findById(3L)).thenReturn(Optional.of(user));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Notification notification = notificationService.scheduleNotification(3L, NotificationMedium.SMS, "Later", Instant.now().plusSeconds(30));

        assertEquals(NotificationStatus.PENDING, notification.getStatus());
        assertNotNull(notification.getScheduledAt());
    }
}
