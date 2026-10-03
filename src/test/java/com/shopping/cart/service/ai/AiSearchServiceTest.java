package com.shopping.cart.service.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopping.cart.dto.response.AiSearchResponse;
import com.shopping.cart.entity.Product;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AiSearchServiceTest {

    private static Product product(String name, long price, String description) {
        Product p = new Product(name, description, BigDecimal.valueOf(price), 10, null, null, false, List.of());
        p.setId(UUID.randomUUID());
        p.setCategory("Herbs & Spices");
        return p;
    }

    @Test
    void sanitizeKeepsPricesAndMyanmarButDropsQuotesAndBraces() {
        assertEquals("spicy dinner under 10,000 Ks", AiSearchService.sanitizeQuery("  spicy \"dinner\"\nunder 10,000 Ks {}"));
        assertEquals("ကြက်သား ဟင်း", AiSearchService.sanitizeQuery("ကြက်သား ဟင်း"));
        assertNull(AiSearchService.sanitizeQuery("  <> "));
        assertEquals(AiSearchService.MAX_QUERY_LENGTH, AiSearchService.sanitizeQuery("a".repeat(500)).length());
    }

    @Test
    void promptIncludesPriceAndShortDescription() {
        String prompt = AiSearchService.buildPrompt("spicy",
                List.of(product("Hot Bird's Eye Chilies", 1200, "Small, fiery chilies.\nGreat for curries")));
        assertTrue(prompt.contains("Shopper request: \"spicy\""));
        assertTrue(prompt.contains("- Hot Bird's Eye Chilies [Herbs & Spices] | 1,200 Ks | Small, fiery chilies. Great for curries"));
    }

    @Test
    void parseKeepsRealProductsInOrderWithoutDuplicates() throws Exception {
        Product chilies = product("Hot Bird's Eye Chilies", 1200, "");
        Product ginger = product("Organic Fresh Ginger Root", 2800, "");
        Map<String, Product> catalog = Map.of(
                "hot bird's eye chilies", chilies,
                "organic fresh ginger root", ginger);
        var reply = new ObjectMapper().readTree("""
                {"summary":"Spicy picks","picks":[
                  {"name":"Organic Fresh Ginger Root [Herbs & Spices]","reason":"warming"},
                  {"name":"Made Up Sriracha","reason":"x"},
                  {"name":"Hot Bird's Eye Chilies","reason":"heat"},
                  {"name":"hot bird's eye chilies","reason":"dupe"}]}
                """);

        AiSearchResponse response = AiSearchService.parseReply(reply, catalog);

        assertEquals("Spicy picks", response.summary());
        assertEquals(List.of(ginger.getId(), chilies.getId()),
                response.products().stream().map(p -> p.product().id()).toList());
        assertEquals("warming", response.products().get(0).reason());
    }

    @Test
    void parseToleratesMissingFields() throws Exception {
        AiSearchResponse response = AiSearchService.parseReply(new ObjectMapper().readTree("{}"), Map.of());
        assertEquals("", response.summary());
        assertTrue(response.products().isEmpty());
    }
}
