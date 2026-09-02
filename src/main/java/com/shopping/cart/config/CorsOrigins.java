package com.shopping.cart.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class CorsOrigins {

    private final List<String> origins;

    public CorsOrigins(
            @Value("${app.frontend.base-url:}") String frontendBaseUrl,
            @Value("${app.admin.base-url:}") String adminBaseUrl,
            Environment environment) {
        List<String> allowed = new ArrayList<>();
        boolean cloudRun = System.getenv("K_SERVICE") != null && !System.getenv("K_SERVICE").isBlank();
        boolean localProfile = Arrays.stream(environment.getActiveProfiles())
                .anyMatch(p -> p.equalsIgnoreCase("local") || p.equalsIgnoreCase("dev"));

        // Only allow localhost origins outside Cloud Run (or when explicitly in local/dev).
        if (!cloudRun || localProfile) {
            allowed.add("http://localhost:3000");
            allowed.add("http://127.0.0.1:3000");
            allowed.add("http://localhost:3001");
            allowed.add("http://127.0.0.1:3001");
            allowed.add("http://localhost");
        }

        addOriginIfPresent(allowed, frontendBaseUrl);
        addOriginIfPresent(allowed, adminBaseUrl);
        this.origins = List.copyOf(allowed);
    }

    private static void addOriginIfPresent(List<String> allowed, String url) {
        if (url == null || url.isBlank()) {
            return;
        }
        String base = url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
        if (!allowed.contains(base)) {
            allowed.add(base);
        }
    }

    public String[] asArray() {
        return origins.toArray(String[]::new);
    }
}
