package com.ecommerce.notification_service.service;

import com.ecommerce.notification_service.rabbit.OrderNotificationMessage;
import com.ecommerce.notification_service.model.FailedNotification;
import com.ecommerce.notification_service.repository.FailedNotificationRepository;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Async;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final JavaMailSender mailSender;
    private final FailedNotificationRepository failedNotificationRepository;
    private final ObjectMapper objectMapper;
    private final TemplateEngine templateEngine;


    @Value("${spring.mail.username}")
    private String emailSender;

    @Async
    public void sendMail(OrderNotificationMessage event) {
        try {
            if (event.getCustomerEmail() == null || event.getCustomerEmail().isBlank()) {
                System.err.println("Email is missing for order: " + event.getOrderId());
                saveFailedNotification(event, "Missing customer email.");
                return;
            }

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(emailSender,"Shoppia Store");
            helper.setTo(event.getCustomerEmail());
            helper.setSubject("Order Confirmation - Order ID: " + event.getOrderId());

            Context context = new Context();
            context.setVariable("orderId", event.getOrderId());
            context.setVariable("price", event.getPrice());
            context.setVariable("transactionId", event.getTransactionId());
            context.setVariable("couponCode", event.getCouponCode());

            String htmlContent = templateEngine.process("email/customer-order", context);

            helper.setText(htmlContent, true);
            mailSender.send(message);

            System.out.println("Email sent successfully for order: " + event.getOrderId());

        } catch (Exception e) {
            System.err.println("Failed to send email for order: " + event.getOrderId() + " - " + e.getMessage());
            saveFailedNotification(event, e.getMessage());
        }
    }


    public void saveFailedNotification(OrderNotificationMessage event, String errorMessage) {
        try {
            Optional<FailedNotification> existing = failedNotificationRepository.findByOrderIdAndSentSuccessfullyFalse(String.valueOf(event.getOrderId()));

            if (existing.isPresent()) {
                FailedNotification failedNotification = existing.get();
                failedNotification.setRetryCount(failedNotification.getRetryCount() + 1);
                failedNotification.setErrorMessage(errorMessage);
                failedNotificationRepository.save(failedNotification);
            } else {
                String eventJson = objectMapper.writeValueAsString(event);

                FailedNotification failedNotification = new FailedNotification();
                failedNotification.setCustomerEmail(event.getCustomerEmail());
                failedNotification.setOrderId(String.valueOf(event.getOrderId()));
                failedNotification.setEventJson(eventJson);
                failedNotification.setRetryCount(1);
                failedNotification.setErrorMessage(errorMessage);
                failedNotification.setSentSuccessfully(false);

                failedNotificationRepository.save(failedNotification);
            }

        } catch (Exception e) {
            System.err.println("Error saving failed notification for order " + event.getOrderId() + ": " + e.getMessage());
        }
    }

}