package com.shopping.cart.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "orders")
public class Order extends BaseEntity {
    private BigDecimal totalPrice;

    @ManyToOne // Each order belongs to a user
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL) // One order can have multiple order items
    private List<OrderItem> orderItems = new ArrayList<>();

    private String status;

    /** Stripe Checkout Session id — unique when set, used for idempotent fulfillment. */
    @Column(name = "stripe_checkout_session_id", unique = true)
    private String stripeCheckoutSessionId;

    // Default constructor is required by JPA
    public Order() {}

    // Parameterized constructor
    public Order(BigDecimal totalPrice, User user) {
        this.totalPrice = totalPrice;
        this.user = user;
    }
}
