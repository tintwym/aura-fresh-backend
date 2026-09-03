package com.shopping.cart.dto.response;

import com.shopping.cart.entity.Notification;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class NotificationResponse {
    private final UUID id;
    private final String title;
    private final String message;
    private final String type;
    private final UUID relatedOrderId;
    private final boolean read;
    private final LocalDateTime createdAt;

    public NotificationResponse(
            UUID id,
            String title,
            String message,
            String type,
            UUID relatedOrderId,
            boolean read,
            LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.type = type;
        this.relatedOrderId = relatedOrderId;
        this.read = read;
        this.createdAt = createdAt;
    }

    public static NotificationResponse from(Notification n) {
        return new NotificationResponse(
                n.getId(),
                n.getTitle(),
                n.getMessage(),
                n.getType(),
                n.getRelatedOrderId(),
                n.isRead(),
                n.getCreatedAt());
    }
}
