package com.shopping.cart.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "profiles")
public class Profile extends BaseEntity {
    private String address1;
    private String address2;
    private String unit;
    private String floor;
    private String city;
    private String state;
    private String country;
    private String zipCode;

    @JsonIgnore
    @OneToOne(mappedBy = "profile") // Each profile belongs to a user
    private User user;

    // Default constructor is required by JPA
    public Profile() {}

    // Parameterized constructor
    public Profile(String address1, String address2, String unit, String floor, String city, String state, String country, String zipCode) {
        this.address1 = address1;
        this.address2 = address2;
        this.unit = unit;
        this.floor = floor;
        this.city = city;
        this.state = state;
        this.country = country;
        this.zipCode = zipCode;
    }
}
