package com.ecommerce.notification_service.job;
import com.ecommerce.notification_service.model.Notification;
import com.ecommerce.notification_service.repository.NotificationRepository;
import com.ecommerce.notification_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class RetryScheduler {

    private final NotificationRepository repository;
    private final NotificationService notificationService;
    private final int MAX_RETRIES = 5;

    @Scheduled(fixedRate = 60000)
    public void retryFailedEmails() {
        List<Notification> failedEvents = repository.findBySentSuccessfullyFalseAndRetryCountLessThan(MAX_RETRIES);

        for (Notification event : failedEvents) {
            notificationService.sendEmailToCustomer(event);

            if (!event.isSentSuccessfully() && event.getRetryCount() >= MAX_RETRIES) {
                notificationService.notifyAdmin(event);
            }
        }
    }
}
