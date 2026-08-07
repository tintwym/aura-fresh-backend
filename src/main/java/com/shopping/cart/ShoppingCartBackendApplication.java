package com.shopping.cart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.shopping.cart.config.JwtProperties;

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
public class ShoppingCartBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(ShoppingCartBackendApplication.class, args);
    }
}
