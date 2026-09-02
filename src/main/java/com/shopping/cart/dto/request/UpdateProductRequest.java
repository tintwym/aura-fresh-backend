package com.shopping.cart.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class UpdateProductRequest {
    private String name;
    private String description;
    private BigDecimal price;
    private int stock;
    private String category;
    private LocalDate expiryDate;
}
