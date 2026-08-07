package com.shopping.cart.utility;

import com.shopping.cart.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtility {
    private final JwtProperties jwtProperties;

    public JwtUtility(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    // Refresh window time (e.g., 15 minutes before expiration)
    private static final long REFRESH_WINDOW = 900000; // 15 minutes in milliseconds
    private static final String CLAIM_TOKEN_VERSION = "tv";

    private SecretKey signingKey() {
        byte[] keyBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
        return new SecretKeySpec(keyBytes, "HmacSHA256");
    }

    public String generateToken(String username) {
        return generateToken(username, 0L);
    }

    public String generateToken(String username, long tokenVersion) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_TOKEN_VERSION, tokenVersion);
        return doGenerateToken(claims, username);
    }

    private String doGenerateToken(Map<String, Object> claims, String subject) {
        Date issuedAt = new Date();
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(issuedAt)
                .expiration(new Date(issuedAt.getTime() + jwtProperties.getExpiration()))
                .signWith(signingKey())
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public long extractTokenVersion(String token) {
        Number version = extractClaim(token, claims -> claims.get(CLAIM_TOKEN_VERSION, Number.class));
        return version != null ? version.longValue() : 0L;
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, java.util.function.Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public boolean shouldRefreshToken(String token) {
        Date expiration = extractExpiration(token);
        long timeToExpiration = expiration.getTime() - System.currentTimeMillis();
        return timeToExpiration <= REFRESH_WINDOW;
    }

    public boolean isTokenValid(String token, String username) {
        final String extractedUsername = extractUsername(token);
        return extractedUsername.equals(username) && !isTokenExpired(token);
    }

    public boolean isTokenValid(String token, String username, long expectedTokenVersion) {
        return isTokenValid(token, username) && extractTokenVersion(token) == expectedTokenVersion;
    }
}
