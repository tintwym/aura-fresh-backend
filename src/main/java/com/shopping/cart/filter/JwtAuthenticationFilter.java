package com.shopping.cart.filter;

import com.shopping.cart.entity.User;
import com.shopping.cart.repository.UserRepository;
import com.shopping.cart.utility.JwtUtility;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtUtility jwtUtility;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(JwtUtility jwtUtility, UserRepository userRepository) {
        this.jwtUtility = jwtUtility;
        this.userRepository = userRepository;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        if (!path.startsWith("/api/")) {
            return true;
        }
        if (path.startsWith("/api/auth/")) {
            return true;
        }
        if ("/api/products/index".equals(path)) {
            return true;
        }
        if (path.startsWith("/api/products/show/")) {
            return true;
        }
        if (path.startsWith("/api/reviews/product/")) {
            return true;
        }
        if (path.startsWith("/api/stripe/webhook")) {
            return true;
        }
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String jwt = authHeader.substring(7);
        String username;
        try {
            username = jwtUtility.extractUsername(jwt);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        User user = userRepository.findByUsername(username);
        if (user == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        if (!jwtUtility.isTokenValid(jwt, username, user.getTokenVersion())) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        request.setAttribute("username", username);

        if (jwtUtility.shouldRefreshToken(jwt)) {
            long tokenVersion = user.getTokenVersion();
            String newToken = jwtUtility.generateToken(username, tokenVersion);
            response.setHeader("Authorization", "Bearer " + newToken);
        }

        filterChain.doFilter(request, response);
    }
}
