package com.shopping.cart.controller.api;

import com.shopping.cart.entity.Order;
import com.shopping.cart.service.OrderService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderApiController {
    private final OrderService orderService;

    public OrderApiController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/history")
    public List<Order> getOrderHistory(@RequestHeader("Authorization") String token) {
        return orderService.getOrderHistory(token);
    }
}
