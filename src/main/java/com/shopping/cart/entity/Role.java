package com.shopping.cart.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "roles")
public class Role extends BaseEntity {
    private String name;

    // Default constructor is required by JPA
    public Role() {}

    // Parameterized constructor
    public Role(String name) {
        this.name = name;
    }
}
