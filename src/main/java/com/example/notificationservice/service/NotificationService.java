package com.example.notificationservice.service;

import com.example.notificationservice.config.NotificationRetryProperties;
import com.example.notificationservice.entity.Notification;
import com.example.notificationservice.entity.NotificationMedium;
import com.example.notificationservice.entity.NotificationStatus;
import com.example.notificationservice.entity.User;
import com.example.notificationservice.exception.NotificationDeliveryException;
import com.example.notificationservice.exception.NotificationNotFoundException;
import com.example.notificationservice.exception.NotificationValidationException;
import com.example.notificationservice.exception.UserNotFoundException;
import com.example.notificationservice.repository.NotificationRepository;
import com.example.notificationservice.repository.UserRepository;
import com.example.notificationservice.strategy.NotificationSender;
import com.example.notificationservice.strategy.NotificationSenderRegistry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class NotificationService {

    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationSenderRegistry senderRegistry;
    private final NotificationRetryProperties retryProperties;

    public NotificationService(UserRepository userRepository,
                              NotificationRepository notificationRepository,
                              NotificationSenderRegistry senderRegistry,
                              NotificationRetryProperties retryProperties) {
        this.userRepository = userRepository;
        this.notificationRepository = notificationRepository;
        this.senderRegistry = senderRegistry;
        this.retryProperties = retryProperties == null ? new NotificationRetryProperties() : retryProperties;
    }

    @Transactional
    public Notification sendImmediateNotification(Long userId, NotificationMedium medium, String message) {
        User user = getUserOrThrow(userId);
        validateMessage(message);
        Notification notification = createNotification(userId, medium, message, Instant.now());
        notificationRepository.save(notification);
        return processNotification(notification, user);
    }

    @Transactional
    public Notification scheduleNotification(Long userId, NotificationMedium medium, String message, Instant scheduledAt) {
        getUserOrThrow(userId);
        validateMessage(message);
        Instant targetTime = scheduledAt == null ? Instant.now() : scheduledAt;
        Notification notification = createNotification(userId, medium, message, targetTime);
        notification.setStatus(NotificationStatus.PENDING);
        return notificationRepository.save(notification);
    }

    @Transactional(readOnly = true)
    public Notification getNotificationById(Long notificationId) {
        return notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotificationNotFoundException(notificationId));
    }

    @Transactional(readOnly = true)
    public List<Notification> getNotificationsByStatus(NotificationStatus status) {
        return notificationRepository.findByStatusOrderByScheduledAtAsc(status);
    }

    @Transactional
    public void processDueNotifications() {
        List<Notification> dueNotifications = notificationRepository.findDueNotifications(
                Instant.now(),
                List.of(NotificationStatus.PENDING, NotificationStatus.RETRYING)
        );

        for (Notification notification : dueNotifications) {
            User user = userRepository.findById(notification.getUserId())
                    .orElseThrow(() -> new UserNotFoundException(notification.getUserId()));
            processNotification(notification, user);
        }
    }

    @Transactional
    public Notification processNotification(Notification notification, User user) {
        NotificationSender sender = senderRegistry.getSender(notification.getMedium());
        notification.setStatus(NotificationStatus.PROCESSING);
        notification = notificationRepository.save(notification);

        try {
            sendWithRetry(notification, user, sender);
            notification.setStatus(NotificationStatus.SENT);
            notification.setLastError(null);
            return notificationRepository.save(notification);
        } catch (NotificationValidationException ex) {
            notification.setStatus(NotificationStatus.FAILED);
            notification.setLastError(ex.getMessage());
            notificationRepository.save(notification);
            throw ex;
        } catch (NotificationDeliveryException ex) {
            notification.setStatus(NotificationStatus.FAILED);
            notification.setLastError(ex.getMessage());
            notificationRepository.save(notification);
            return notification;
        }
    }

    private void sendWithRetry(Notification notification, User user, NotificationSender sender) {
        int attempts = 0;
        while (attempts < retryProperties.getMaxAttempts()) {
            try {
                sender.send(user, notification.getMessage());
                return;
            } catch (NotificationValidationException ex) {
                throw ex;
            } catch (RuntimeException ex) {
                attempts++;
                notification.setRetryCount(attempts);
                if (attempts >= retryProperties.getMaxAttempts()) {
                    throw new NotificationDeliveryException("Delivery failed after " + attempts + " attempts", ex);
                }
                notification.setStatus(NotificationStatus.RETRYING);
                notification.setLastError(ex.getMessage());
                notificationRepository.save(notification);
                sleepBeforeRetry();
            }
        }
    }

    private void sleepBeforeRetry() {
        long delay = retryProperties.getDelay();
        if (delay > 0) {
            try {
                Thread.sleep(delay);
            } catch (InterruptedException interruptedException) {
                Thread.currentThread().interrupt();
                throw new NotificationDeliveryException("Retry interrupted", interruptedException);
            }
        }
    }

    private User getUserOrThrow(Long userId) {
        if (userId == null) {
            throw new NotificationValidationException("userId is required");
        }
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    private void validateMessage(String message) {
        if (message == null || message.isBlank()) {
            throw new NotificationValidationException("message cannot be blank");
        }
    }

    private Notification createNotification(Long userId, NotificationMedium medium, String message, Instant scheduledAt) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setMedium(medium);
        notification.setMessage(message);
        notification.setStatus(NotificationStatus.PENDING);
        notification.setScheduledAt(scheduledAt);
        return notification;
    }
}
