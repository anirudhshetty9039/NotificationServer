package com.example.notificationservice.scheduler;

import com.example.notificationservice.config.NotificationSchedulerProperties;
import com.example.notificationservice.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class NotificationScheduler {

    private static final Logger log = LoggerFactory.getLogger(NotificationScheduler.class);

    private final NotificationService notificationService;
    private final NotificationSchedulerProperties properties;

    public NotificationScheduler(NotificationService notificationService, NotificationSchedulerProperties properties) {
        this.notificationService = notificationService;
        this.properties = properties;
    }

    @Scheduled(fixedDelayString = "${notification.scheduler.delay}")
    public void processPendingNotifications() {
        log.info("Running scheduler to process pending notifications");
        notificationService.processDueNotifications();
    }

    public long getDelay() {
        return properties.getDelay();
    }
}
