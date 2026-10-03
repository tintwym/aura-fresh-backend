package com.shopping.cart.dto.response;

import com.shopping.cart.dto.response.RecipeSuggestionResponse.ShopProduct;

import java.util.List;

/**
 * AI search answer: a one-line summary plus real in-stock products, each with why it fits.
 */
public record AiSearchResponse(String summary, List<Pick> products) {
    public record Pick(ShopProduct product, String reason) {}
}
