package com.shopping.cart.controller.api;

import com.shopping.cart.dto.request.AiSearchRequest;
import com.shopping.cart.dto.request.RecipeSuggestionRequest;
import com.shopping.cart.dto.response.AiSearchResponse;
import com.shopping.cart.dto.response.RecipeSuggestionResponse;
import com.shopping.cart.service.ai.AiRateLimiter;
import com.shopping.cart.service.ai.AiSearchService;
import com.shopping.cart.service.ai.RecipeSuggestionService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

/** Public so guests on the web shop can use AI features; rate-limited per client IP instead. */
@RestController
@RequestMapping("/api/ai")
public class AiApiController {
    private final RecipeSuggestionService recipeSuggestionService;
    private final AiSearchService aiSearchService;
    private final AiRateLimiter rateLimiter;

    public AiApiController(
            RecipeSuggestionService recipeSuggestionService,
            AiSearchService aiSearchService,
            AiRateLimiter rateLimiter) {
        this.recipeSuggestionService = recipeSuggestionService;
        this.aiSearchService = aiSearchService;
        this.rateLimiter = rateLimiter;
    }

    @PostMapping("/recipes")
    public ResponseEntity<Map<String, List<RecipeSuggestionResponse>>> suggestRecipes(
            @RequestBody(required = false) RecipeSuggestionRequest request,
            HttpServletRequest httpRequest) {
        if (!rateLimiter.tryAcquire("recipes:" + clientKey(httpRequest))) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Too many recipe requests. Try again shortly.");
        }
        List<String> items = request == null ? null : request.getItems();
        return ResponseEntity.ok(Map.of("recipes", recipeSuggestionService.suggest(items)));
    }

    @PostMapping("/search")
    public ResponseEntity<AiSearchResponse> search(
            @RequestBody(required = false) AiSearchRequest request,
            HttpServletRequest httpRequest) {
        if (!rateLimiter.tryAcquire("search:" + clientKey(httpRequest))) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Too many AI searches. Try again shortly.");
        }
        return ResponseEntity.ok(aiSearchService.search(request == null ? null : request.getQuery()));
    }

    /** Cloud Run puts the caller's IP first in X-Forwarded-For. */
    private static String clientKey(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
