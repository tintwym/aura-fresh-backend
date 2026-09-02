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

    /** Snapshot of delivery address at checkout time. */
    @Column(name = "delivery_address1")
    private String deliveryAddress1;
    @Column(name = "delivery_address2")
    private String deliveryAddress2;
    @Column(name = "delivery_unit")
    private String deliveryUnit;
    @Column(name = "delivery_floor")
    private String deliveryFloor;
    @Column(name = "delivery_city")
    private String deliveryCity;
    @Column(name = "delivery_state")
    private String deliveryState;
    @Column(name = "delivery_country")
    private String deliveryCountry;
    @Column(name = "delivery_zip_code")
    private String deliveryZipCode;

    // Default constructor is required by JPA
    public Order() {}

    // Parameterized constructor
    public Order(BigDecimal totalPrice, User user) {
        this.totalPrice = totalPrice;
        this.user = user;
    }
}
