package com.shopping.cart.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "reviews")
public class Review extends BaseEntity {
    private String comment;
    private int rating;

    @ManyToOne // Each review is associated with a product
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne // Each review is associated with a user who made the purchase
    @JoinColumn(name = "user_id")
    private User user;

    @OneToOne // One review belongs to one order item
    @JoinColumn(name = "order_item_id")
    private OrderItem orderItem;

    // Default constructor
    public Review() {}

    // Parameterized constructor
    public Review(String comment, int rating, Product product, User user, OrderItem orderItem) {
        this.comment = comment;
        this.rating = rating;
        this.product = product;
        this.user = user;
        this.orderItem = orderItem;
    }
}
