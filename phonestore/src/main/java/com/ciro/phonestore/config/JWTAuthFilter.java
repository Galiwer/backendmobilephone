package com.ciro.phonestore.config;

import com.ciro.phonestore.services.JWTService;
import com.ciro.phonestore.services.OurUserDetailsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class JWTAuthFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(JWTAuthFilter.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private JWTService jwtService;

    @Autowired
    private OurUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = authHeader.substring(7);
            String username = jwtService.extractUsername(token);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                if (jwtService.isTokenValid(token, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities());

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    logger.debug("User authenticated: {} with roles: {}", username, userDetails.getAuthorities());
                }
            }
        } catch (Exception e) {
            logger.error("Cannot set user authentication: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private void sendAuthenticationError(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        Map<String, String> error = new HashMap<>();
        error.put("error", "Authentication failed");
        error.put("message", message);

        objectMapper.writeValue(response.getOutputStream(), error);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        logger.debug("Checking if should filter request to: {}", path);

        boolean shouldNotFilter = path.startsWith("/auth/") ||
                path.startsWith("/public/") ||
                path.startsWith("/api/products/list") ||
                path.startsWith("/api/products/get/") ||
                path.startsWith("/api/jobs/status/") ||
                path.startsWith("/api/jobs/track/") ||
                path.startsWith("/api/jobs/public/") ||
                path.startsWith("/images/") ||
                path.startsWith("/api/faqs/published") ||
                path.startsWith("/api/firmware/view/") ||
                path.startsWith("/api/firmware/brands") ||
                path.startsWith("/api/firmware/models/") ||
                path.equals("/error") ||
                path.startsWith("/actuator/") ||
                path.startsWith("/v3/api-docs/") ||
                path.startsWith("/swagger-ui/") ||
                path.equals("/swagger-ui.html") ||
                "OPTIONS".equalsIgnoreCase(request.getMethod());

        if (shouldNotFilter) {
            logger.debug("Skipping JWT filter for path: {}", path);
        } else {
            logger.debug("Will apply JWT filter for path: {}", path);
        }

        return shouldNotFilter;
    }

    private boolean isProtectedEndpoint(String path) {

        if (path.startsWith("/auth/") ||
                path.startsWith("/public/") ||
                path.startsWith("/api/products/list") ||
                path.startsWith("/api/products/get/") ||
                path.startsWith("/api/jobs/status/") ||
                path.startsWith("/api/jobs/track/") ||
                path.startsWith("/api/jobs/public/") ||
                path.startsWith("/images/") ||
                path.startsWith("/api/faqs/published") ||
                path.startsWith("/api/firmware/view/") ||
                path.startsWith("/api/firmware/brands") ||
                path.startsWith("/api/firmware/models/") ||
                path.equals("/error") ||
                path.startsWith("/actuator/") ||
                path.startsWith("/v3/api-docs/") ||
                path.startsWith("/swagger-ui/") ||
                path.equals("/swagger-ui.html")) {
            return false;
        }

        // Then check if it's a protected endpoint
        return path.startsWith("/api/jobs/") ||
                path.startsWith("/admin/") ||
                path.startsWith("/dashboard/") ||
                path.startsWith("/api/admin/");
    }
}
