package com.shopping.cart.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateProfileRequest {
    private String address1;
    private String address2;
    private String unit;
    private String floor;
    private String city;
    private String state;
    private String country;
    private String zipCode;
}
