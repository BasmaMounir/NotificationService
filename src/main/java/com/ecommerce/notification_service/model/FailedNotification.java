package com.ecommerce.notification_service.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
public class FailedNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String orderId;
    private String customerEmail;

    @Column(columnDefinition = "TEXT")
    private String eventJson;

    private int retryCount;
    private String errorMessage;

    private boolean sentSuccessfully;

}
