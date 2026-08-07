package com.shopping.cart.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * Parses cloud {@code DATABASE_URL} (postgresql://…) into a JDBC DataSource.
 * When unset, Spring Boot uses {@code spring.datasource.*} from properties.
 */
@Configuration
@ConditionalOnExpression("T(org.springframework.util.StringUtils).hasText('${DATABASE_URL:}')")
public class DatabaseConfig {

    @Bean
    @Primary
    public DataSource dataSource(@Value("${DATABASE_URL}") String databaseUrl) {
        String normalized = databaseUrl.startsWith("jdbc:")
                ? databaseUrl
                : toJdbcUrl(databaseUrl);

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(normalized);
        config.setDriverClassName("org.postgresql.Driver");
        config.setMaximumPoolSize(5);
        config.setMinimumIdle(1);
        config.setConnectionTimeout(30_000);

        if (!databaseUrl.startsWith("jdbc:")) {
            URI uri = URI.create(databaseUrl.replace("postgres://", "postgresql://"));
            if (uri.getUserInfo() != null) {
                String[] userInfo = uri.getUserInfo().split(":", 2);
                config.setUsername(decode(userInfo[0]));
                if (userInfo.length > 1) {
                    config.setPassword(decode(userInfo[1]));
                }
            }
        }

        return new HikariDataSource(config);
    }

    private static String toJdbcUrl(String databaseUrl) {
        String normalized = databaseUrl.replace("postgres://", "postgresql://");
        URI uri = URI.create(normalized);
        int port = uri.getPort() > 0 ? uri.getPort() : 5432;
        String query = uri.getQuery();
        if (query == null || !query.contains("sslmode=")) {
            query = query == null ? "sslmode=require" : query + "&sslmode=require";
        }
        return "jdbc:postgresql://" + uri.getHost() + ":" + port + uri.getPath() + "?" + query;
    }

    private static String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }
}
