package com.shopping.cart.service.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopping.cart.dto.response.RecipeSuggestionResponse;
import com.shopping.cart.entity.Product;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RecipeSuggestionServiceTest {

    private static Product product(String name, int stock) {
        Product p = new Product(name, "", BigDecimal.valueOf(2500), stock, null, null, false, List.of());
        p.setId(UUID.randomUUID());
        p.setCategory("Fresh Vegetables");
        return p;
    }

    @Test
    void sanitizeKeepsMyanmarAndDropsPromptCharacters() {
        assertEquals("ကြက်သွန်ဖြူ Garlic", RecipeSuggestionService.sanitizeItemName("  ကြက်သွန်ဖြူ\nGarlic "));
        assertEquals("Rice ignore previous", RecipeSuggestionService.sanitizeItemName("Rice {ignore} <previous>"));
        assertNull(RecipeSuggestionService.sanitizeItemName("   "));
        assertNull(RecipeSuggestionService.sanitizeItemName("x".repeat(81)));
    }

    @Test
    void parseKeepsOnlyRealCatalogProducts() throws Exception {
        Product tomato = product("Vine-Ripened Inle Tomatoes", 40);
        Map<String, Product> catalog = Map.of("vine-ripened inle tomatoes", tomato);
        JsonNode reply = new ObjectMapper().readTree("""
                [{"name":"Tomato Curry","description":"d","cookingTime":"20 min","difficulty":"Easy",
                  "matchingIngredients":["Rice"],"missingIngredients":["Tomato","Saffron"],
                  "shopProducts":["vine-ripened inle tomatoes","Vine-Ripened Inle Tomatoes","Made Up Saffron"],
                  "instructions":["Cook"]},
                 {"description":"missing name is skipped"}]
                """);

        List<RecipeSuggestionResponse> recipes = RecipeSuggestionService.parseRecipes(reply, catalog);

        assertEquals(1, recipes.size());
        RecipeSuggestionResponse recipe = recipes.get(0);
        assertEquals("Tomato Curry", recipe.name());
        assertEquals(List.of("Tomato", "Saffron"), recipe.missingIngredients());
        assertEquals(1, recipe.shopProducts().size());
        assertEquals(tomato.getId(), recipe.shopProducts().get(0).id());
    }

    @Test
    void catalogMatchToleratesCategorySuffixAndShortNames() {
        Product ginger = product("Organic Fresh Ginger Root", 10);
        Product chilies = product("Hot Bird's Eye Chilies", 10);
        Product greenChilies = product("Green Chilies", 10);
        Map<String, Product> catalog = Map.of(
                "organic fresh ginger root", ginger,
                "hot bird's eye chilies", chilies,
                "green chilies", greenChilies);

        assertSame(ginger, RecipeSuggestionService.findCatalogProduct("Organic Fresh Ginger Root [Herbs & Spices]", catalog));
        assertSame(ginger, RecipeSuggestionService.findCatalogProduct("Fresh Ginger", catalog));
        assertNull(RecipeSuggestionService.findCatalogProduct("Chilies", catalog), "ambiguous names are dropped");
        assertNull(RecipeSuggestionService.findCatalogProduct("Oil", catalog), "very short names are not fuzzy-matched");
    }

    @Test
    void parseToleratesNonArrayReply() throws Exception {
        assertTrue(RecipeSuggestionService.parseRecipes(new ObjectMapper().readTree("{}"), Map.of()).isEmpty());
    }

    @Test
    void promptListsCatalogWithCategories() {
        String prompt = RecipeSuggestionService.buildPrompt(List.of("Rice"), List.of(product("Fresh Green Cabbage", 5)));
        assertTrue(prompt.contains("Rice"));
        assertTrue(prompt.contains("- Fresh Green Cabbage [Fresh Vegetables]"));
    }

    @Test
    void rateLimiterBlocksAfterLimit() {
        AiRateLimiter limiter = new AiRateLimiter();
        for (int i = 0; i < AiRateLimiter.LIMIT_PER_WINDOW; i++) {
            assertTrue(limiter.tryAcquire("1.2.3.4"));
        }
        assertFalse(limiter.tryAcquire("1.2.3.4"));
        assertTrue(limiter.tryAcquire("5.6.7.8"));
    }
}
