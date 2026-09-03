package com.shopping.cart.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "notifications")
public class Notification extends BaseEntity {

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 1000)
    private String message;

    /** order | info | success | warning */
    @Column(nullable = false, length = 32)
    private String type = "order";

    @Column(name = "related_order_id")
    private UUID relatedOrderId;

    @Column(name = "read", nullable = false)
    private boolean read = false;

    public Notification() {}

    public Notification(User user, String title, String message, String type, UUID relatedOrderId) {
        this.user = user;
        this.title = title;
        this.message = message;
        this.type = type;
        this.relatedOrderId = relatedOrderId;
    }
}
