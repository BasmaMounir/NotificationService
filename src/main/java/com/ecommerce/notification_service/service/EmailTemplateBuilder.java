package com.ecommerce.notification_service.service;

import com.ecommerce.notification_service.rabbit.order.OrderEvent;
import com.ecommerce.notification_service.rabbit.user.UserEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class EmailTemplateBuilder {

    private final TemplateEngine templateEngine;

    public String buildOrderConfirmationEmail(OrderEvent event) {
        Context context = new Context();
        context.setVariable("customerName", event.getCustomerName());
        context.setVariable("orderId", event.getOrderId());
        context.setVariable("price", event.getPrice());
        context.setVariable("transactionId", event.getTransactionId());
        context.setVariable("couponCode", event.getCouponCode());

        return templateEngine.process("email/customer-order", context);
    }

    public String buildPasswordConfirmationCode(UserEvent event) {
        Context context = new Context();
        context.setVariable("customerName", event.getCustomerEmail());
        context.setVariable("code", event.getCode());

        return templateEngine.process("email/user-pass", context);
    }

    public String buildAdminAlertEmail(Map<String, Object> model) {
        Context context = new Context();
        context.setVariables(model);
        return templateEngine.process("email/admin-alert", context);
    }
}


