package com.shopping.cart.service.social;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class GoogleIdTokenVerifier {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient;
    private final Set<String> acceptedAudiences;

    public GoogleIdTokenVerifier(
            @Value("${app.auth.google.client-id:}") String googleClientId,
            @Value("${app.auth.google.client-ids:}") String googleClientIds) {
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(8)).build();
        this.acceptedAudiences = parseAudiences(googleClientId, googleClientIds);
    }

    public SocialIdentity verify(String idToken) {
        if (acceptedAudiences.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Google Sign-In is not configured (set GOOGLE_CLIENT_ID)");
        }
        if (idToken == null || idToken.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing Google ID token");
        }

        try {
            String url = "https://oauth2.googleapis.com/tokeninfo?id_token="
                    + URLEncoder.encode(idToken, StandardCharsets.UTF_8);
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Google ID token");
            }

            JsonNode json = objectMapper.readTree(response.body());
            String aud = text(json, "aud");
            if (aud == null || !acceptedAudiences.contains(aud)) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Google token audience mismatch");
            }

            String iss = text(json, "iss");
            if (iss == null || !(
                    "accounts.google.com".equals(iss)
                            || "https://accounts.google.com".equals(iss))) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Google token issuer");
            }

            String sub = text(json, "sub");
            if (sub == null || sub.isBlank()) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Google token missing subject");
            }

            String email = text(json, "email");
            boolean emailVerified = booleanClaim(json, "email_verified");
            String given = text(json, "given_name");
            String family = text(json, "family_name");
            String name = text(json, "name");
            if ((given == null || given.isBlank()) && name != null) {
                String[] parts = name.trim().split("\\s+", 2);
                given = parts[0];
                family = parts.length > 1 ? parts[1] : "";
            }

            return new SocialIdentity(
                    sub,
                    email,
                    given != null && !given.isBlank() ? given : "Aura",
                    family != null && !family.isBlank() ? family : "Shopper",
                    emailVerified
            );
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Could not verify Google ID token");
        }
    }

    private static Set<String> parseAudiences(String primary, String extras) {
        String combined = ((primary == null ? "" : primary) + "," + (extras == null ? "" : extras));
        return Arrays.stream(combined.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
    }

    private static String text(JsonNode node, String field) {
        JsonNode v = node.get(field);
        if (v == null || v.isNull()) return null;
        String s = v.asText();
        return s == null || s.isBlank() ? null : s;
    }

    /** Google tokeninfo may return email_verified as boolean or "true"/"false". */
    private static boolean booleanClaim(JsonNode node, String field) {
        JsonNode v = node.get(field);
        if (v == null || v.isNull()) return false;
        if (v.isBoolean()) return v.asBoolean();
        return "true".equalsIgnoreCase(v.asText());
    }
}
