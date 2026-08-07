package com.shopping.cart.service;

import com.shopping.cart.entity.Order;
import com.shopping.cart.entity.User;
import com.shopping.cart.interfaces.IOrderService;
import com.shopping.cart.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrderService implements IOrderService {
    private final OrderRepository orderRepository;
    private final UserService userService;

    public OrderService(OrderRepository orderRepository, UserService userService) {
        this.orderRepository = orderRepository;
        this.userService = userService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getOrderHistory(String token) {
        User user = userService.requireUser(token);
        return orderRepository.findByUserWithItems(user);
    }
}
