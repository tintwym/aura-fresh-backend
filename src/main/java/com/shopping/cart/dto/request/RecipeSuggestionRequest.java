package com.shopping.cart.dto.request;

import java.util.List;

public class RecipeSuggestionRequest {
    /** Names of the grocery items currently in the shopper's cart. */
    private List<String> items;

    public List<String> getItems() {
        return items;
    }

    public void setItems(List<String> items) {
        this.items = items;
    }
}
