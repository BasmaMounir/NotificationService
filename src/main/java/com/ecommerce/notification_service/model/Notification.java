package com.ecommerce.notification_service.model;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long orderId;
    private String customerEmail;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(columnDefinition = "TEXT")
    private String eventJson;

    private int retryCount;

    @Lob
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
    private boolean sentSuccessfully;

}
