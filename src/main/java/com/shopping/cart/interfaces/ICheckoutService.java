package com.shopping.cart.interfaces;

import com.shopping.cart.dto.response.CheckoutSessionResponse;

public interface ICheckoutService {
    CheckoutSessionResponse checkout(String token);

    /** Confirms a paid Stripe Checkout Session for the current user (idempotent). */
    void confirmCheckoutSession(String token, String sessionId);
}
