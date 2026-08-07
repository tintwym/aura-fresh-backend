//Author name: Wei YiTao
//Author student ID:
//Function:
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
@Table(name = "cart_items")
public class CartItem extends BaseEntity {
    @JsonIgnore
    @ManyToOne // Each cart item belongs to a cart
    @JoinColumn(name = "cart_id")
    private Cart cart;

    @ManyToOne // Each cart item references a product
    @JoinColumn(name = "product_id")
    private Product product;

    private int quantity;
    private BigDecimal price;

    // Default constructor is required by JPA
    public CartItem() {}

    // Parameterized constructor
    public CartItem(Cart cart, Product product, int quantity, BigDecimal price) {
        this.cart = cart;
        this.product = product;
        this.quantity = quantity;
        this.price = price;
    }
}
