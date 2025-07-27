package com.ecommerce.notification_service.rabbit;

import com.ecommerce.notification_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderMessageListener {

    private final NotificationService notificationService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE)
    public String receiveOrderMessage(OrderEvent event) {
        notificationService.handleOrderEvent(event);
        return "Email sending process initiated. You will receive an email shortly.";
    }
}
