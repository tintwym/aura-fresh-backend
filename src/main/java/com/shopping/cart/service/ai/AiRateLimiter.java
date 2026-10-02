package com.shopping.cart.service.ai;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Fixed-window, per-client limiter for paid AI calls. In-memory only, so each Cloud Run
 * instance keeps its own window — good enough to stop one client from draining the Gemini quota.
 */
@Component
public class AiRateLimiter {
    static final int LIMIT_PER_WINDOW = 10;
    static final long WINDOW_MS = 60_000;
    private static final int MAX_TRACKED_CLIENTS = 10_000;

    private record Window(int count, long resetAt) {}

    private final Map<String, Window> windows = new ConcurrentHashMap<>();

    public boolean tryAcquire(String clientKey) {
        long now = System.currentTimeMillis();
        if (windows.size() > MAX_TRACKED_CLIENTS) {
            windows.values().removeIf(w -> now > w.resetAt());
        }
        Window updated = windows.compute(clientKey, (key, current) ->
                current == null || now > current.resetAt()
                        ? new Window(1, now + WINDOW_MS)
                        : new Window(current.count() + 1, current.resetAt()));
        return updated.count() <= LIMIT_PER_WINDOW;
    }
}
