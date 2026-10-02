package com.shopping.cart.service.ai;

import com.shopping.cart.dto.response.RecipeSuggestionResponse;
import com.shopping.cart.entity.Product;
import com.shopping.cart.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** Calls the real Gemini API; runs only when GEMINI_API_KEY is set. */
@EnabledIfEnvironmentVariable(named = "GEMINI_API_KEY", matches = ".+")
class RecipeSuggestionLiveTest {

    private static Product product(String name, String category) {
        Product p = new Product(name, "", BigDecimal.valueOf(3000), 20, null, null, false, List.of());
        p.setId(UUID.randomUUID());
        p.setCategory(category);
        return p;
    }

    @Test
    void suggestsRecipesWithRealCatalogProducts() {
        ProductRepository repository = mock(ProductRepository.class);
        when(repository.findByIsDeletedFalseWithImages()).thenReturn(List.of(
                product("Vine-Ripened Inle Tomatoes", "Fresh Vegetables"),
                product("Organic Fresh Ginger Root", "Herbs & Spices"),
                product("Hot Bird's Eye Chilies", "Herbs & Spices"),
                product("Traditional Fish Sauce", "Sauces & Condiments"),
                product("Cold-Pressed Peanut Oil", "Cooking Oils"),
                product("Shwe Bo Paw San Premium Rice", "Rice & Grains")
        ));
        GeminiClient client = new GeminiClient(System.getenv("GEMINI_API_KEY"),
                System.getenv().getOrDefault("GEMINI_MODEL", "gemini-3.8-flash"),
                System.getenv().getOrDefault("GEMINI_FALLBACK_MODELS", "gemini-3.6-flash,gemini-flash-latest"));

        List<RecipeSuggestionResponse> recipes = new RecipeSuggestionService(client, repository)
                .suggest(List.of("Free-Range Chicken Thighs", "Shwe Bo Paw San Premium Rice"));

        assertFalse(recipes.isEmpty());
        recipes.forEach(r -> {
            assertFalse(r.instructions().isEmpty(), r.name() + " has steps");
            r.shopProducts().forEach(p -> assertNotEquals("Shwe Bo Paw San Premium Rice", p.name(), "cart items are not re-suggested"));
            System.out.println("RECIPE " + r.name() + " | shop=" + r.shopProducts().stream().map(RecipeSuggestionResponse.ShopProduct::name).toList());
        });
    }
}
