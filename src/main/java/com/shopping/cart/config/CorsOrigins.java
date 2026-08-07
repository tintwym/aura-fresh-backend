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
            Environment environment) {
        List<String> allowed = new ArrayList<>();
        boolean cloudRun = System.getenv("K_SERVICE") != null && !System.getenv("K_SERVICE").isBlank();
        boolean localProfile = Arrays.stream(environment.getActiveProfiles())
                .anyMatch(p -> p.equalsIgnoreCase("local") || p.equalsIgnoreCase("dev"));

        // Only allow localhost origins outside Cloud Run (or when explicitly in local/dev).
        if (!cloudRun || localProfile) {
            allowed.add("http://localhost:3000");
        }

        if (frontendBaseUrl != null && !frontendBaseUrl.isBlank()) {
            String base = frontendBaseUrl.endsWith("/")
                    ? frontendBaseUrl.substring(0, frontendBaseUrl.length() - 1)
                    : frontendBaseUrl;
            if (!allowed.contains(base)) {
                allowed.add(base);
            }
        }
        this.origins = List.copyOf(allowed);
    }

    public String[] asArray() {
        return origins.toArray(String[]::new);
    }
}
