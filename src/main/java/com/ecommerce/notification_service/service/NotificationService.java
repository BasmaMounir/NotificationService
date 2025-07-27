package com.ecommerce.notification_service.service;

import com.ecommerce.notification_service.model.Notification;
import com.ecommerce.notification_service.model.Status;
import com.ecommerce.notification_service.rabbit.OrderEvent;
import com.ecommerce.notification_service.repository.NotificationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository repository;
    private final EmailSenderService emailSender;
    private final EmailTemplateBuilder templateBuilder;
    private final ObjectMapper objectMapper;

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$"
    );

    @Transactional
    public void handleOrderEvent(OrderEvent orderEvent) {
        try {
            log.info("Handling order event for orderId={}", orderEvent.getOrderId());

            if (!isValidEmail(orderEvent.getCustomerEmail())) {
                log.warn("Invalid email format: {}", orderEvent.getCustomerEmail());
            }

            String eventJson = objectMapper.writeValueAsString(orderEvent);

            Notification event = Notification.builder()
                    .orderId(orderEvent.getOrderId())
                    .customerEmail(orderEvent.getCustomerEmail())
                    .eventJson(eventJson)
                    .retryCount(0)
                    .sentSuccessfully(false)
                    .status(Status.PENDING)
                    .build();

            repository.save(event);
            log.info("Notification saved to DB for orderId={}", orderEvent.getOrderId());

            sendEmailToCustomer(event);

        } catch (Exception ex) {
            log.error("Error while handling order event", ex);
        }
    }

    public void sendEmailToCustomer(Notification event) {
        try {
            log.info("Sending email to customer: {}", event.getCustomerEmail());

            OrderEvent orderEvent = objectMapper.readValue(event.getEventJson(), OrderEvent.class);
            String htmlContent = templateBuilder.buildOrderConfirmationEmail(orderEvent);

            emailSender.sendHtmlEmail(
                    event.getCustomerEmail(),
                    "✅ Order Confirmation",
                    htmlContent
            );

            event.setSentSuccessfully(true);
            event.setStatus(Status.SUCCESS);
            log.info("Email sent successfully to {}", event.getCustomerEmail());

        } catch (Exception ex) {
            log.error("Failed to send email to {}", event.getCustomerEmail(), ex);

            event.setRetryCount(event.getRetryCount() + 1);
            event.setErrorMessage(ex.getMessage());
            event.setStatus(Status.FAILED);
        }

        repository.save(event);
    }

    public void notifyAdmin(Notification event) {
        try {
            log.info("Notifying admin about failed notification for orderId={}", event.getOrderId());

            Map<String, Object> model = Map.of(
                    "orderId", event.getOrderId(),
                    "customerEmail", event.getCustomerEmail(),
                    "errorMessage", event.getErrorMessage()
            );

            String htmlContent = templateBuilder.buildAdminAlertEmail(model);

            emailSender.sendHtmlEmail(
                    "iotsec10@gmail.com",
                    "❌ Failed Email Notification",
                    htmlContent
            );

            log.info("Admin notified successfully.");

        } catch (Exception e) {
            log.error("Failed to notify admin", e);
        }
    }

    private boolean isValidEmail(String email) {
        return EMAIL_PATTERN.matcher(email).matches();
    }
}
