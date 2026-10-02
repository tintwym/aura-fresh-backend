package com.shopping.cart.dto.response;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * One AI recipe idea. {@code shopProducts} are real, in-stock catalog products that cover
 * missing ingredients, so clients can add them to the cart by id.
 */
public record RecipeSuggestionResponse(
        String name,
        String description,
        String cookingTime,
        String difficulty,
        List<String> matchingIngredients,
        List<String> missingIngredients,
        List<String> instructions,
        List<ShopProduct> shopProducts
) {
    public record ShopProduct(
            UUID id,
            String name,
            BigDecimal price,
            int stock,
            String category,
            String imageUrl
    ) {}
}
