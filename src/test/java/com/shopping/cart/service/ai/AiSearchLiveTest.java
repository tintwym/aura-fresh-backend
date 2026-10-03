package com.shopping.cart.service.ai;

import com.shopping.cart.dto.response.AiSearchResponse;
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
class AiSearchLiveTest {

    private static Product product(String name, String category, long price) {
        Product p = new Product(name, "", BigDecimal.valueOf(price), 20, null, null, false, List.of());
        p.setId(UUID.randomUUID());
        p.setCategory(category);
        return p;
    }

    @Test
    void findsProductsForANaturalLanguageRequest() {
        ProductRepository repository = mock(ProductRepository.class);
        when(repository.findByIsDeletedFalseWithImages()).thenReturn(List.of(
                product("Hot Bird's Eye Chilies", "Herbs & Spices", 1200),
                product("Traditional Fish Sauce", "Sauces & Condiments", 3100),
                product("Crispy Butter Cookies", "Snacks", 4800),
                product("Natural Organic Coconut Water", "Beverages", 2900),
                product("Halal Free-Range Whole Chicken", "Poultry", 12500)
        ));
        GeminiClient client = new GeminiClient(System.getenv("GEMINI_API_KEY"),
                System.getenv().getOrDefault("GEMINI_MODEL", "gemini-3.8-flash"),
                System.getenv().getOrDefault("GEMINI_FALLBACK_MODELS", "gemini-3.6-flash,gemini-flash-latest"));

        AiSearchResponse response = new AiSearchService(client, repository).search("something spicy for a curry");

        assertFalse(response.products().isEmpty());
        assertTrue(response.products().stream().noneMatch(p -> p.product().name().equals("Crispy Butter Cookies")));
        System.out.println("SUMMARY " + response.summary());
        response.products().forEach(p -> System.out.println("PICK " + p.product().name() + " | " + p.reason()));
    }
}
