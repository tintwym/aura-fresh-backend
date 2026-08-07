package com.shopping.cart.interfaces;

import com.shopping.cart.entity.Order;

import java.util.List;
import java.util.UUID;

public interface IOrderService {
    List<Order> getOrderHistory(String token);

    List<Order> getAllOrdersForAdmin(String token);

    Order updateOrderStatusForAdmin(String token, UUID orderId, String status);
}
