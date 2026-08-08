package com.shopping.cart.service.social;

import com.shopping.cart.dto.request.SocialLoginRequest;
import com.shopping.cart.dto.response.AuthResponse;
import com.shopping.cart.entity.AuthProvider;
import com.shopping.cart.entity.Role;
import com.shopping.cart.entity.User;
import com.shopping.cart.repository.RoleRepository;
import com.shopping.cart.repository.UserRepository;
import com.shopping.cart.utility.JwtUtility;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
public class SocialAuthService {
    private final GoogleIdTokenVerifier googleIdTokenVerifier;
    private final AppleIdTokenVerifier appleIdTokenVerifier;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JwtUtility jwtUtility;

    public SocialAuthService(
            GoogleIdTokenVerifier googleIdTokenVerifier,
            AppleIdTokenVerifier appleIdTokenVerifier,
            UserRepository userRepository,
            RoleRepository roleRepository,
            JwtUtility jwtUtility) {
        this.googleIdTokenVerifier = googleIdTokenVerifier;
        this.appleIdTokenVerifier = appleIdTokenVerifier;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.jwtUtility = jwtUtility;
    }

    @Transactional
    public AuthResponse loginWithSocial(SocialLoginRequest request) {
        AuthProvider provider = parseProvider(request.getProvider());
        SocialIdentity identity = switch (provider) {
            case GOOGLE -> googleIdTokenVerifier.verify(request.getIdToken());
            case APPLE -> appleIdTokenVerifier.verify(request.getIdToken());
            case LOCAL -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported provider");
        };
        identity = applyClientNameHints(identity, request);

        Optional<User> bySubject = userRepository.findByProviderAndProviderSubject(provider, identity.subject());
        if (bySubject.isPresent()) {
            User existing = bySubject.get();
            maybeUpdateNames(existing, identity);
            return tokenFor(existing);
        }

        String normalizedEmail = normalizeEmail(identity.email());
        if (normalizedEmail != null) {
            if (!identity.emailVerified()) {
                throw new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Verified email is required for social sign-in");
            }

            User byEmail = findByEmailIgnoreCase(normalizedEmail);
            if (byEmail != null) {
                AuthProvider existingProvider = byEmail.getProvider() == null
                        ? AuthProvider.LOCAL
                        : byEmail.getProvider();
                if (existingProvider != AuthProvider.LOCAL && existingProvider != provider) {
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "Email is already linked to a different sign-in method");
                }
                if (byEmail.getProviderSubject() != null
                        && !byEmail.getProviderSubject().equals(identity.subject())) {
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "Email is already linked to another account");
                }
                // Attach provider to existing local (or same-provider) account
                byEmail.setProvider(provider);
                byEmail.setProviderSubject(identity.subject());
                maybeUpdateNames(byEmail, identity);
                userRepository.save(byEmail);
                return tokenFor(byEmail);
            }
        }

        if (normalizedEmail == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Email is required for first-time social sign-in");
        }
        if (!identity.emailVerified()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Verified email is required for social sign-in");
        }

        User created = createSocialUser(provider, identity, normalizedEmail);
        return tokenFor(created);
    }

    private User createSocialUser(AuthProvider provider, SocialIdentity identity, String email) {
        Role role = roleRepository.findByName("User");
        if (role == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "User role is not configured");
        }

        User user = new User();
        user.setFirstName(identity.firstName());
        user.setLastName(identity.lastName());
        user.setEmail(email);
        user.setUsername(uniqueUsername(provider, identity.subject(), email));
        user.setPassword(null);
        user.setProvider(provider);
        user.setProviderSubject(identity.subject());
        user.setRole(role);
        return userRepository.save(user);
    }

    private User findByEmailIgnoreCase(String normalizedEmail) {
        User exact = userRepository.findByEmail(normalizedEmail);
        if (exact != null) {
            return exact;
        }
        return userRepository.findByEmailIgnoreCase(normalizedEmail);
    }

    private static SocialIdentity applyClientNameHints(SocialIdentity identity, SocialLoginRequest request) {
        String first = blankToNull(request.getFirstName());
        String last = blankToNull(request.getLastName());
        if (first == null && last == null) {
            return identity;
        }
        boolean placeholderFirst = identity.firstName() == null
                || identity.firstName().isBlank()
                || "Aura".equals(identity.firstName());
        boolean placeholderLast = identity.lastName() == null
                || identity.lastName().isBlank()
                || "Shopper".equals(identity.lastName());
        return new SocialIdentity(
                identity.subject(),
                identity.email(),
                first != null && placeholderFirst ? first : identity.firstName(),
                last != null && placeholderLast ? last : identity.lastName(),
                identity.emailVerified()
        );
    }

    private void maybeUpdateNames(User user, SocialIdentity identity) {
        boolean dirty = false;
        if ((user.getFirstName() == null || user.getFirstName().isBlank() || "Aura".equals(user.getFirstName()))
                && identity.firstName() != null
                && !identity.firstName().isBlank()
                && !"Aura".equals(identity.firstName())) {
            user.setFirstName(identity.firstName());
            dirty = true;
        }
        if ((user.getLastName() == null || user.getLastName().isBlank() || "Shopper".equals(user.getLastName()))
                && identity.lastName() != null
                && !identity.lastName().isBlank()
                && !"Shopper".equals(identity.lastName())) {
            user.setLastName(identity.lastName());
            dirty = true;
        }
        if (dirty) {
            userRepository.save(user);
        }
    }

    private String uniqueUsername(AuthProvider provider, String subject, String email) {
        String local = email.contains("@") ? email.substring(0, email.indexOf('@')) : email;
        String cleaned = local.replaceAll("[^a-zA-Z0-9._-]", "");
        if (cleaned.length() < 3) {
            cleaned = provider.name().toLowerCase(Locale.ROOT) + "_user";
        }
        if (cleaned.length() > 24) {
            cleaned = cleaned.substring(0, 24);
        }

        String candidate = cleaned;
        int i = 0;
        while (userRepository.findByUsername(candidate) != null) {
            String suffix = String.valueOf(++i);
            String base = cleaned.length() + suffix.length() > 28
                    ? cleaned.substring(0, Math.max(3, 28 - suffix.length()))
                    : cleaned;
            candidate = base + suffix;
            if (i > 50) {
                candidate = provider.name().toLowerCase(Locale.ROOT) + "_"
                        + subject.substring(0, Math.min(12, subject.length()))
                        + "_" + UUID.randomUUID().toString().substring(0, 6);
                if (userRepository.findByUsername(candidate) == null) {
                    break;
                }
                candidate = provider.name().toLowerCase(Locale.ROOT) + "_"
                        + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
                break;
            }
        }
        return candidate;
    }

    private AuthResponse tokenFor(User user) {
        if (user.getRole() == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Account cannot sign in");
        }
        String roleName = user.getRole().getName();
        if (!"User".equalsIgnoreCase(roleName) && !"Admin".equalsIgnoreCase(roleName)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Account cannot sign in");
        }
        return new AuthResponse(jwtUtility.generateToken(user.getUsername(), user.getTokenVersion()));
    }

    private static AuthProvider parseProvider(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Provider is required");
        }
        try {
            AuthProvider provider = AuthProvider.valueOf(raw.trim().toUpperCase(Locale.ROOT));
            if (provider == AuthProvider.LOCAL) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported provider");
            }
            return provider;
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported provider");
        }
    }

    private static String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private static String blankToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
