package com.shopping.cart.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * Fails before the app starts when Cloud Run is missing required secrets.
 */
public class ProductionDatabaseEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final String DEFAULT_JWT_SECRET = "local-dev-secret-key-for-jwt-testing-32b";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (!isCloudRun()) {
            return;
        }
        String databaseUrl = environment.getProperty("DATABASE_URL");
        if (databaseUrl == null || databaseUrl.isBlank()) {
            throw new IllegalStateException(
                    "DATABASE_URL is not set. In Google Cloud Run → service → Variables, "
                            + "add your Neon PostgreSQL URL "
                            + "(postgresql://user:pass@host/db?sslmode=require), then redeploy.");
        }

        String jwtSecret = environment.getProperty("JWT_SECRET");
        if (jwtSecret == null || jwtSecret.isBlank() || DEFAULT_JWT_SECRET.equals(jwtSecret)) {
            throw new IllegalStateException(
                    "JWT_SECRET must be set to a unique value (min 32 characters) on Cloud Run. "
                            + "Do not deploy with the local development default.");
        }
        if (jwtSecret.length() < 32) {
            throw new IllegalStateException("JWT_SECRET must be at least 32 characters.");
        }
    }

    private static boolean isCloudRun() {
        return isSet(System.getenv("K_SERVICE"));
    }

    private static boolean isSet(String value) {
        return value != null && !value.isBlank();
    }
}
