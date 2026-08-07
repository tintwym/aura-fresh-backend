package com.shopping.cart.interfaces;

import com.shopping.cart.entity.Cart;

import java.util.UUID;

public interface ICartService {
    int getCartItemCount(String token);
    Cart getCartByUser(String token);
    void addProductToCart(String token, UUID productId, int quantity);
    void updateProductInCart(String token, UUID productId, int quantity);
    void deleteProductFromCart(String token, UUID productId);
}
