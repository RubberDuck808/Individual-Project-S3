package nl.fontys.db3.backend.config;

import nl.fontys.db3.backend.security.DeviceApiKeyAuthFilter;
import nl.fontys.db3.backend.security.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final DeviceApiKeyAuthFilter deviceApiKeyAuthFilter;
    private final CorsConfigurationSource corsConfigurationSource;

    public SecurityConfig(
            JwtAuthFilter jwtAuthFilter,
            DeviceApiKeyAuthFilter deviceApiKeyAuthFilter,
            CorsConfigurationSource corsConfigurationSource
    ) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.deviceApiKeyAuthFilter = deviceApiKeyAuthFilter;
        this.corsConfigurationSource = corsConfigurationSource;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            .csrf(AbstractHttpConfigurer::disable)

            .sessionManagement(sess ->
                sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/users/login").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/users/register").permitAll()
                // Actuator health endpoint for Docker health checks
                .requestMatchers("/actuator/health").permitAll()
                // Public endpoints - user profiles, hazards, vote counts, friendships
                .requestMatchers(HttpMethod.GET, "/api/users/{username}").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/hazards/open").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/hazards/by-user/{username}").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/votes/{hazardId}/count").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/friendships/user/{username}").permitAll()
                .requestMatchers("/ws/**").permitAll()
                // Device registration requires user authentication
                .requestMatchers(HttpMethod.POST, "/api/devices/register").authenticated()
                // Telemetry endpoints are authenticated via the DeviceApiKeyAuthFilter.
                // The filter validates the device API key header and blocks unauthorized requests.
                // permitAll() here bypasses Spring Security's JWT check; the device filter is the gate.
                .requestMatchers("/api/telemetry/live", "/api/telemetry/history").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/telemetry/live/**").permitAll()
                // Admin endpoints require ADMIN role
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )

            // Add custom filters in order:
            // 1. Device API key filter (for telemetry endpoints) - runs first
            // 2. JWT filter (for user authentication) - runs after device filter
            // Both before UsernamePasswordAuthenticationFilter
            .addFilterBefore(deviceApiKeyAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            
            // Configure authentication entry point to return 401 instead of 403
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(HttpStatus.UNAUTHORIZED.value());
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\":\"Unauthorized\"}");
                })
            );

        return http.build();
    }


    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
