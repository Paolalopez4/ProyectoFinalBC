package org.pasantia.ahorraya.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.pasantia.ahorraya.dto.response.ErrorResponse;
import org.pasantia.ahorraya.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.time.Instant;

/**
 * Security configuration for the application.
 *
 * <p>Configures HTTP security rules, stateless session management, password encoding,
 * the JWT authentication filter and custom handlers for access-denied and authentication
 * failures. Public API endpoints (docs and auth) are permitted; other endpoints require
 * appropriate roles as configured in the request matchers.</p>
 */
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    /**
     * Filter responsible for extracting and validating JWT tokens from incoming requests.
     */
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Jackson ObjectMapper used to serialize error responses as JSON.
     */
    private final ObjectMapper objectMapper;

    /**
     * Exposes the application's AuthenticationManager.
     *
     * @param authConfig injected AuthenticationConfiguration
     * @return AuthenticationManager used by Spring Security
     * @throws Exception when the authentication manager cannot be obtained
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * Password encoder bean using BCrypt hashing.
     *
     * @return PasswordEncoder instance for hashing passwords
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configures the security filter chain: URL authorization, session policy,
     * exception handlers and registers the JWT authentication filter.
     *
     * @param http the HttpSecurity to configure
     * @return configured SecurityFilterChain
     * @throws Exception when configuration fails
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()
                        .requestMatchers(
                                "/api/auth/**"
                        ).permitAll()
                        .requestMatchers("/api/auth/register", "/api/auth/login", "/api/auth/validate").permitAll()
                        .requestMatchers("/api/auth/register-admin").permitAll()
                        .requestMatchers("/error").permitAll()

                        .requestMatchers("/api/users/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.GET, "/api/saving-accounts/user/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/saving-accounts/user/*/credit").hasRole("USER")
                        .requestMatchers(HttpMethod.POST, "/api/saving-accounts/user/*/debit").hasRole("USER")
                        .requestMatchers(HttpMethod.DELETE, "/api/saving-accounts/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.GET, "/api/expenses/user/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/expenses").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/expenses/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/expenses/**").hasAnyRole("USER", "ADMIN")

                        .requestMatchers(HttpMethod.POST, "/api/micro-saving-configs/*/activate").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/micro-saving-configs/*/deactivate").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/micro-saving-configs/user/*/active").hasAnyRole("USER", "ADMIN")

                        .requestMatchers(HttpMethod.POST, "/api/saving-movements/credit").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/saving-movements/debit").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/saving-movements/user/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/saving-movements/user/*/stats").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/saving-movements/user/*/monthly-evolution").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/saving-movements/user/*/savings").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/saving-movements/user/*/savings-range").hasAnyRole("USER", "ADMIN")

                        .requestMatchers(HttpMethod.GET, "/api/admins/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/admins").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/admins/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/admins/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/admins/**").hasRole("ADMIN")

                        .anyRequest().authenticated()
                )
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .exceptionHandling(ex -> ex
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(HttpStatus.FORBIDDEN.value());
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);

                            ErrorResponse error = ErrorResponse.builder()
                                    .timestamp(Instant.now())
                                    .status(HttpStatus.FORBIDDEN.value())
                                    .error("Forbidden")
                                    .message("You do not have permission to access this resource")
                                    .path(request.getRequestURI())
                                    .build();

                            response.getWriter().write(objectMapper.writeValueAsString(error));
                        })

                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpStatus.UNAUTHORIZED.value());
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);

                            ErrorResponse error = ErrorResponse.builder()
                                    .timestamp(Instant.now())
                                    .status(HttpStatus.UNAUTHORIZED.value())
                                    .error("Unauthorized")
                                    .message("Invalid token or session expired. Please log in again.")
                                    .path(request.getRequestURI())
                                    .build();

                            response.getWriter().write(objectMapper.writeValueAsString(error));
                        })
                );

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}