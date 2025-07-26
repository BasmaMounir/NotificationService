package com.ecommerce.notification_service.job;

import com.ecommerce.notification_service.model.FailedNotification;
import com.ecommerce.notification_service.rabbit.OrderNotificationMessage;
import com.ecommerce.notification_service.repository.FailedNotificationRepository;
import com.ecommerce.notification_service.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class FailedNotificationRetryJob {

    private final FailedNotificationRepository failedNotificationRepository;
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    // Every 1 minute
    @Scheduled(fixedRate = 60 * 1000)
    public void retryFailedNotifications() {
        int maxRetries = 5;
        List<FailedNotification> failedNotifications = failedNotificationRepository
                .findBySentSuccessfullyFalseAndRetryCountLessThan(maxRetries);

        for (FailedNotification failedNotification : failedNotifications) {
            try {
                OrderNotificationMessage event = objectMapper.readValue(failedNotification.getEventJson(), OrderNotificationMessage.class);

                notificationService.sendMail(event);

                failedNotification.setSentSuccessfully(true);

                failedNotificationRepository.save(failedNotification);

            } catch (Exception e) {
                int newRetry = failedNotification.getRetryCount() + 1;
                failedNotification.setRetryCount(newRetry);

                if (newRetry >= maxRetries) {
                    System.out.println("Max retries reached for order");
                }

                failedNotificationRepository.save(failedNotification);
            }
        }
    }

}
