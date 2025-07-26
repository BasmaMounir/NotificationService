package com.ecommerce.notification_service.repository;

import com.ecommerce.notification_service.model.FailedNotification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FailedNotificationRepository extends JpaRepository<FailedNotification, Long> {

    List<FailedNotification> findBySentSuccessfullyFalseAndRetryCountLessThan(int maxRetries);
    Optional<FailedNotification> findByOrderIdAndSentSuccessfullyFalse(String orderId);

}
