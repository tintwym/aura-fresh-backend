package com.shopping.cart.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterAdminRequest {
    private String firstName;
    private String lastName;
    private String username;
    private String email;
    private String password;
}
