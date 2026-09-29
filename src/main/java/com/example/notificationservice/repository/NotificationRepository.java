package com.example.notificationservice.repository;

import com.example.notificationservice.entity.Notification;
import com.example.notificationservice.entity.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query("SELECT n FROM Notification n WHERE n.status IN :statuses AND n.scheduledAt <= :now ORDER BY n.scheduledAt ASC")
    List<Notification> findDueNotifications(@Param("now") Instant now,
                                           @Param("statuses") Collection<NotificationStatus> statuses);

    List<Notification> findByStatusOrderByScheduledAtAsc(NotificationStatus status);
}
