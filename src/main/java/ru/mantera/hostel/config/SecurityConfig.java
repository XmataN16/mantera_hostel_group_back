package ru.mantera.hostel.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import java.util.Arrays;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration
    ) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())

                .cors(Customizer.withDefaults())

                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        ).permitAll()

                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()

                        /*
                         * Публичная часть будущего сайта отеля на Angular.
                         * Здесь можно смотреть отели, категории номеров,
                         * тарифы и доступность без входа в систему.
                         */
                        .requestMatchers(HttpMethod.GET, "/api/hotels/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/room-types/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/rate-plans/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/reservations/availability").permitAll()

                        .requestMatchers("/api/auth/me").authenticated()

                        .requestMatchers(
                                "/api/employees/**",
                                "/api/users/**",
                                "/api/roles/**"
                        ).hasRole("ADMIN")

                        .requestMatchers("/api/reports/**")
                        .hasAnyRole("ADMIN", "MANAGER", "ACCOUNTANT")

                        .requestMatchers(
                                "/api/room-type-rates/**",
                                "/api/additional-services/**"
                        ).hasAnyRole("ADMIN", "MANAGER")

                        .requestMatchers(HttpMethod.PATCH, "/api/rooms/*/housekeeping-status")
                        .hasAnyRole("ADMIN", "MANAGER", "HOUSEKEEPING")

                        .requestMatchers("/api/rooms/**")
                        .hasAnyRole("ADMIN", "MANAGER", "HOUSEKEEPING")

                        .requestMatchers(
                                "/api/reservations/*/payments",
                                "/api/reservations/*/payments/**",
                                "/api/reservations/*/invoice"
                        ).hasAnyRole("ADMIN", "MANAGER", "RECEPTIONIST", "ACCOUNTANT")

                        .requestMatchers(
                                "/api/reservations/*/services",
                                "/api/reservations/*/services/**"
                        ).hasAnyRole("ADMIN", "MANAGER", "RECEPTIONIST")

                        .requestMatchers("/api/reservations/**")
                        .hasAnyRole("ADMIN", "MANAGER", "RECEPTIONIST")

                        .requestMatchers("/api/guests/**")
                        .hasAnyRole("ADMIN", "MANAGER", "RECEPTIONIST")

                        .requestMatchers("/api/hotels/**")
                        .hasAnyRole("ADMIN", "MANAGER")

                        .anyRequest().authenticated()
                )

                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwt ->
                                jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())
                        )
                );

        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            List<String> roles = getRoles(jwt);

            return roles.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .collect(Collectors.toList());
        });

        return converter;
    }

    private List<String> getRoles(Jwt jwt) {
        Object rolesClaim = jwt.getClaims().get("roles");

        if (rolesClaim instanceof List<?> roles) {
            return roles.stream()
                    .map(String::valueOf)
                    .toList();
        }

        return List.of();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        return new CorsConfigurationSource() {
            @Override
            public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
                CorsConfiguration config = new CorsConfiguration();

                config.setAllowedOrigins(
                        Arrays.stream(allowedOrigins.split(","))
                                .map(String::trim)
                                .filter(origin -> !origin.isBlank())
                                .toList()
                );

                config.setAllowedMethods(List.of(
                        "GET",
                        "POST",
                        "PUT",
                        "PATCH",
                        "DELETE",
                        "OPTIONS"
                ));

                config.setAllowedHeaders(List.of(
                        "Authorization",
                        "Content-Type"
                ));

                config.setExposedHeaders(List.of(
                        "Authorization"
                ));

                config.setAllowCredentials(true);

                return config;
            }
        };
    }


}