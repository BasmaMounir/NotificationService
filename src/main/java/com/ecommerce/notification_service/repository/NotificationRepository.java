package com.ecommerce.notification_service.repository;

import com.ecommerce.notification_service.model.Notification;
import com.ecommerce.notification_service.model.Status;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findBySentSuccessfullyFalseAndRetryCountLessThan(int maxRetries);
    List<Notification> findByStatus(Status status);
    long countByStatus(Status status);
}
