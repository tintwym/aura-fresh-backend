package com.shopping.cart.interfaces;

import com.shopping.cart.entity.Order;

import java.util.List;

public interface IOrderService {
    List<Order> getOrderHistory(String token);
}
