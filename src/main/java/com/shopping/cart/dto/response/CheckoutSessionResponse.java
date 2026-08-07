package com.shopping.cart.dto.response;

import lombok.Getter;

@Getter
public class CheckoutSessionResponse {
    private final String sessionId;
    private final String checkoutUrl;

    public CheckoutSessionResponse(String sessionId, String checkoutUrl) {
        this.sessionId = sessionId;
        this.checkoutUrl = checkoutUrl;
    }
}
