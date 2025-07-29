package com.ecommerce.notification_service.rabbit.order;

import com.ecommerce.notification_service.rabbit.RabbitMQConfig;
import com.ecommerce.notification_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderMessageListener {

    private final NotificationService notificationService;

    @RabbitListener(queues = RabbitMQConfig.ORDER_QUEUE)
    public void receiveOrderMessage(OrderEvent event) {
        notificationService.handleOrderEvent(event);
    }
}

