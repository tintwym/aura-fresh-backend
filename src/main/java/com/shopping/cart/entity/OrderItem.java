package com.shopping.cart.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "order_items")
public class OrderItem extends BaseEntity {
    private int quantity;
    private BigDecimal price;

    @JsonIgnore
    @ManyToOne  // Each order item belongs to an order
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne // Each order item references a product
    @JoinColumn(name = "product_id")
    private Product product;

    // Default constructor is required by JPA
    public OrderItem() {}

    // Parameterized constructor
    public OrderItem(int quantity, BigDecimal price, Order order, Product product) {
        this.quantity = quantity;
        this.price = price;
        this.order = order;
        this.product = product;
    }
}
