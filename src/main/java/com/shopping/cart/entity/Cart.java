//Author name: Wei YiTao
//Author student ID:
//Description:
//
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
@Table(name = "carts")
public class Cart extends BaseEntity {
    private BigDecimal totalPrice;

    @OneToOne // One cart belongs to one user
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true) // One cart can have multiple cart items
    private List<CartItem> cartItems = new ArrayList<>();

    // Default constructor is required by JPA
    public Cart() {}

    // Parameterized constructor
    public Cart(BigDecimal totalPrice, User user) {
        this.totalPrice = totalPrice;
        this.user = user;
    }
}
