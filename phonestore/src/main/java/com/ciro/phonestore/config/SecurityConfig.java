package com.ciro.phonestore.config;

import com.ciro.phonestore.services.OurUserDetailsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    @Autowired
    private OurUserDetailsService ourUserDetailsService;
    @Autowired
    private JWTAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        try {
            logger.info("Configuring security filter chain...");

            httpSecurity
                    .csrf(AbstractHttpConfigurer::disable)
                    .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                    .exceptionHandling(exception -> exception
                            .authenticationEntryPoint((request, response, authException) -> {
                                logger.error("Unauthorized error: {}", authException.getMessage());
                                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Error: Unauthorized");
                            }))
                    .sessionManagement(session -> session
                            .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                    .authorizeHttpRequests(request -> {
                        logger.debug("Configuring authorization rules...");
                        request
                                .requestMatchers(
                                        "/auth/**",
                                        "/auth/login",
                                        "/auth/register",
                                        "/public/**",
                                        "/api/products/list",
                                        "/api/products/get/**",
                                        "/api/jobs/status/**", // Public job status endpoint
                                        "/api/jobs/track/**", // Public job tracking
                                        "/images/**",
                                        "/api/faqs/published",
                                        "/api/firmware/view/**",
                                        "/api/firmware/brands",
                                        "/api/firmware/models/**",
                                        "/error",
                                        "/actuator/**",
                                        "/actuator/health/**",
                                        "/v3/api-docs/**",
                                        "/swagger-ui/**",
                                        "/swagger-ui.html")
                                .permitAll()
                                .requestMatchers(
                                        "/admin/**",
                                        "/admin/register",
                                        "/admin/get-all-users",
                                        "/admin/get-users/**",
                                        "/admin/update/**",
                                        "/admin/delete/**",
                                        "/api/products",
                                        "/api/products/update/**",
                                        "/api/products/delete/**",
                                        "/api/jobs/create/**", // Admin job creation
                                        "/api/jobs/update/**", // Admin job updates
                                        "/api/jobs/delete/**", // Admin job deletion
                                        "/api/jobs/manage/**", // Admin job management
                                        "/api/firmware/upload",
                                        "/api/firmware/delete/**",
                                        "/api/firmware/update/**",
                                        "/api/faqs/**",
                                        "/dashboard/**", // Admin dashboard endpoints
                                        "/api/admin/**") // Admin API endpoints
                                .hasAuthority("ADMIN")
                                .requestMatchers(
                                        "/api/jobs/create", // Authenticated users can create jobs
                                        "/api/jobs/my/**", // Users can view their own jobs
                                        "/user/**",
                                        "/api/user/**") // User-specific endpoints
                                .hasAnyAuthority("USER", "ADMIN")
                                .requestMatchers(
                                        "/adminuser/get-profile",
                                        "/adminuser/**")
                                .hasAnyAuthority("ADMIN", "USER")
                                .anyRequest()
                                .authenticated();
                    })
                    .authenticationProvider(authenticationProvider())
                    .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

            logger.info("Security filter chain configured successfully");
            return httpSecurity.build();
        } catch (Exception e) {
            logger.error("Error configuring security filter chain", e);
            throw e;
        }
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
                "https://mobilephoneshop.vercel.app",
                "http://localhost:5173" // Development frontend URL
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH", "HEAD"));
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "X-Requested-With",
                "Accept",
                "Origin",
                "Access-Control-Request-Method",
                "Access-Control-Request-Headers"));
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Disposition"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        logger.info("CORS configuration initialized with allowed origins: {}", configuration.getAllowedOrigins());
        return source;
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        try {
            logger.debug("Configuring authentication provider...");
            DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
            daoAuthenticationProvider.setUserDetailsService(ourUserDetailsService);
            daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
            logger.info("Authentication provider configured successfully");
            return daoAuthenticationProvider;
        } catch (Exception e) {
            logger.error("Error configuring authentication provider", e);
            throw e;
        }
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        try {
            logger.debug("Creating authentication manager...");
            return authenticationConfiguration.getAuthenticationManager();
        } catch (Exception e) {
            logger.error("Error creating authentication manager", e);
            throw e;
        }
    }
}
