package com.shopping.cart.controller.api;

import com.shopping.cart.dto.response.CheckoutSessionResponse;
import com.shopping.cart.service.CheckoutService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class CheckoutApiController {

    private final CheckoutService checkoutService;

    public CheckoutApiController(CheckoutService checkoutService) {
        this.checkoutService = checkoutService;
    }

    @PostMapping("/checkout")
    public Map<String, String> checkout(@RequestHeader("Authorization") String token) {
        CheckoutSessionResponse session = checkoutService.checkout(token);
        Map<String, String> body = new HashMap<>();
        body.put("sessionId", session.getSessionId());
        body.put("checkoutUrl", session.getCheckoutUrl());
        return body;
    }

    @PostMapping("/checkout/confirm")
    public Map<String, String> confirmCheckout(
            @RequestHeader("Authorization") String token,
            @RequestParam("sessionId") String sessionId) {
        checkoutService.confirmCheckoutSession(token, sessionId);
        Map<String, String> body = new HashMap<>();
        body.put("status", "ok");
        return body;
    }
}
