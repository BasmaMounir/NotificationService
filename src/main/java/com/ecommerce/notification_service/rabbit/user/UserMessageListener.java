package com.ecommerce.notification_service.rabbit.user;

import com.ecommerce.notification_service.rabbit.RabbitMQConfig;
import com.ecommerce.notification_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserMessageListener {

    private final NotificationService notificationService;

    @RabbitListener(queues = RabbitMQConfig.USER_QUEUE)
    public void receiveCodeMessage(UserEvent event) {
        System.out.println("📩 Received user event: " + event);
        notificationService.sendCode(event);
    }
}

