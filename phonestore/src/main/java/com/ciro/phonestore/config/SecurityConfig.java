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
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import jakarta.servlet.http.HttpServletResponse;

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
                    .cors(Customizer.withDefaults())
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
                                        "/auth/login",
                                        "/public/**",
                                        "/api/products/**",
                                        "/images/**",
                                        "/api/faqs/**",
                                        "/api/faqs/published",
                                        "/job/**",
                                        "/jobs",
                                        "/api/firmware/**",
                                        "/firmware/**",
                                        "/api/firmware/brands",
                                        "/api/firmware/models/**",
                                        "/api/firmware/device-data",
                                        "/error",
                                        "/actuator/health")
                                .permitAll()
                                .requestMatchers(
                                        "/admin/**",
                                        "/admin/register",
                                        "/admin/get-all-users",
                                        "/admin/get-users/**",
                                        "/admin/update/**",
                                        "/admin/delete/**")
                                .hasAuthority("ADMIN")
                                .requestMatchers("/user/**")
                                .hasAuthority("USER")
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

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("*")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .maxAge(3600);
            }
        };
    }
}
