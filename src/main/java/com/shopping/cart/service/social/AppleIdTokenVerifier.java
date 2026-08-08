package com.shopping.cart.service.social;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigInteger;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class AppleIdTokenVerifier {
    private static final String APPLE_JWKS_URL = "https://appleid.apple.com/auth/keys";
    private static final String APPLE_ISSUER = "https://appleid.apple.com";

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final Set<String> acceptedAudiences;
    private final Map<String, RSAPublicKey> keyCache = new ConcurrentHashMap<>();
    private volatile Instant keysFetchedAt = Instant.EPOCH;

    public AppleIdTokenVerifier(
            ObjectMapper objectMapper,
            @Value("${app.auth.apple.client-id:}") String appleClientId,
            @Value("${app.auth.apple.client-ids:}") String appleClientIds) {
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(8)).build();
        this.acceptedAudiences = parseAudiences(appleClientId, appleClientIds);
    }

    public SocialIdentity verify(String idToken) {
        if (acceptedAudiences.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Apple Sign-In is not configured (set APPLE_CLIENT_ID)");
        }
        if (idToken == null || idToken.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing Apple ID token");
        }

        try {
            String kid = headerKid(idToken);
            RSAPublicKey publicKey = resolveKey(kid);
            Claims claims = Jwts.parser()
                    .verifyWith(publicKey)
                    .requireIssuer(APPLE_ISSUER)
                    .build()
                    .parseSignedClaims(idToken)
                    .getPayload();

            Set<String> audiences = claims.getAudience();
            String aud = audiences == null
                    ? null
                    : audiences.stream().filter(a -> a != null && !a.isBlank()).findFirst().orElse(null);
            if (aud == null || !acceptedAudiences.contains(aud)) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Apple token audience mismatch");
            }

            String sub = claims.getSubject();
            if (sub == null || sub.isBlank()) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Apple token missing subject");
            }

            String email = claims.get("email", String.class);
            Object emailVerifiedRaw = claims.get("email_verified");
            boolean emailVerified = emailVerifiedRaw == null
                    || Boolean.TRUE.equals(emailVerifiedRaw)
                    || "true".equalsIgnoreCase(String.valueOf(emailVerifiedRaw));

            return new SocialIdentity(sub, email, "Aura", "Shopper", emailVerified);
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Could not verify Apple ID token");
        }
    }

    private String headerKid(String idToken) throws Exception {
        String[] parts = idToken.split("\\.");
        if (parts.length < 2) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Malformed Apple ID token");
        }
        byte[] decoded = Base64.getUrlDecoder().decode(parts[0]);
        JsonNode header = objectMapper.readTree(decoded);
        String kid = header.path("kid").asText(null);
        if (kid == null || kid.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Apple token missing key id");
        }
        return kid;
    }

    private RSAPublicKey resolveKey(String kid) throws Exception {
        RSAPublicKey cached = keyCache.get(kid);
        if (cached != null && keysFetchedAt.isAfter(Instant.now().minus(Duration.ofHours(12)))) {
            return cached;
        }
        refreshKeys();
        RSAPublicKey key = keyCache.get(kid);
        if (key == null) {
            refreshKeys();
            key = keyCache.get(kid);
        }
        if (key == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Apple signing key not found");
        }
        return key;
    }

    private synchronized void refreshKeys() throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create(APPLE_JWKS_URL))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Could not load Apple public keys");
        }
        JsonNode keys = objectMapper.readTree(response.body()).path("keys");
        keyCache.clear();
        for (JsonNode keyNode : keys) {
            if (!"RSA".equals(keyNode.path("kty").asText())) {
                continue;
            }
            // Apple keys usually set use=sig; some JWKS entries omit `use`.
            JsonNode useNode = keyNode.get("use");
            if (useNode != null && !useNode.isNull() && !"sig".equals(useNode.asText())) {
                continue;
            }
            String kid = keyNode.path("kid").asText(null);
            String n = keyNode.path("n").asText(null);
            String e = keyNode.path("e").asText(null);
            if (kid == null || n == null || e == null) continue;
            keyCache.put(kid, toRsaPublicKey(n, e));
        }
        keysFetchedAt = Instant.now();
    }

    private static RSAPublicKey toRsaPublicKey(String n, String e) throws Exception {
        byte[] modulusBytes = Base64.getUrlDecoder().decode(n);
        byte[] exponentBytes = Base64.getUrlDecoder().decode(e);
        RSAPublicKeySpec spec = new RSAPublicKeySpec(
                new BigInteger(1, modulusBytes),
                new BigInteger(1, exponentBytes)
        );
        PublicKey key = KeyFactory.getInstance("RSA").generatePublic(spec);
        return (RSAPublicKey) key;
    }

    private static Set<String> parseAudiences(String primary, String extras) {
        String combined = ((primary == null ? "" : primary) + "," + (extras == null ? "" : extras));
        return Arrays.stream(combined.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
    }
}
