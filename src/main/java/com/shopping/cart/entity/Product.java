package com.shopping.cart.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "products")
public class Product extends BaseEntity {
    @Column(unique = true)
    private String name;

    @Column(length = 1000)
    private String description;
    private BigDecimal price;
    private int stock;

    // Stripe product and price IDs (internal — not exposed on public product JSON)
    @com.fasterxml.jackson.annotation.JsonIgnore
    @Column(name = "stripe_product_id", unique = true)
    private String stripeProductId;

    @com.fasterxml.jackson.annotation.JsonIgnore
    @Column(name = "stripe_price_id", unique = true)
    private String stripePriceId;

    private boolean isDeleted = false;

    // One-to-Many relationship with ProductImage
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductImage> images;

    // Default constructor is required by JPA
    public Product() {}

    // Parameterized constructor
    public Product(String name, String description, BigDecimal price, int stock, String stripeProductId, String stripePriceId, boolean isDeleted, List<ProductImage> images) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.stripeProductId = stripeProductId;
        this.stripePriceId = stripePriceId;
        this.isDeleted = isDeleted;
        this.images = images;
    }
}
