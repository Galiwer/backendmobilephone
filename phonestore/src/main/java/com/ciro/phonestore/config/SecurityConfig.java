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
                                        "/public/**",
                                        "/api/products/list",
                                        "/api/products/get/**",
                                        "/api/jobs/status/**",
                                        "/api/jobs/track/**",
                                        "/api/jobs/public/**",
                                        "/api/firmware/brands",
                                        "/api/firmware/models/**",
                                        "/api/firmware/view/**",
                                        "/api/firmware/download/**",
                                        "/api/firmware/admin/list",
                                        "/images/**",
                                        "/api/faqs/published",
                                        "/error",
                                        "/actuator/**",
                                        "/v3/api-docs/**",
                                        "/swagger-ui/**",
                                        "/swagger-ui.html")
                                .permitAll()

                                .requestMatchers(
                                        "/admin/**",
                                        "/api/products/update/**",
                                        "/api/products/delete/**",
                                        "/api/jobs",
                                        "/api/jobs/{id}",
                                        "/api/jobs/create/**",
                                        "/api/jobs/update/**",
                                        "/api/jobs/delete/**",
                                        "/api/jobs/manage/**",
                                        "/api/firmware/upload",
                                        "/api/firmware/delete/**",
                                        "/api/firmware/update/**",
                                        "/api/firmware/admin/**",
                                        "/api/faqs/**",
                                        "/dashboard/**",
                                        "/api/admin/**")
                                .hasAuthority("ADMIN")

                                .requestMatchers(
                                        "/api/jobs/create",
                                        "/api/jobs/my/**",
                                        "/api/jobs/user/**",
                                        "/user/**",
                                        "/api/user/**",
                                        "/adminuser/**")
                                .hasAnyAuthority("USER", "ADMIN")

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
                "http://localhost:5173"));
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
