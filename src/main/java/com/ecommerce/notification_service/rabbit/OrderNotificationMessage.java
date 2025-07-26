package com.ecommerce.notification_service.rabbit;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderNotificationMessage {
    private Long orderId;
    private String customerEmail;
    private String couponCode;
    private BigDecimal price;
    private Long transactionId;
}