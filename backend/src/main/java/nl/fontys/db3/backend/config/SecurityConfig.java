package nl.fontys.db3.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Enable CORS (this pulls your CorsConfigurationSource bean)
            .cors(Customizer.withDefaults())

            // Disable CSRF (safe for token/JSON APIs)
            .csrf(AbstractHttpConfigurer::disable)

            // Allow all requests for now (public backend)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/users/**").permitAll()
                .requestMatchers("/api/votes/**").permitAll()
                .requestMatchers("/api/hazards/**").permitAll()
                .requestMatchers("/api/hazard-categories/**").permitAll()
                .anyRequest().permitAll()
            )

            // Avoid issues with Cloud Run proxying HTTPS
            .headers(headers -> headers
                .frameOptions(frame -> frame.sameOrigin())   
                .httpStrictTransportSecurity(hsts -> hsts.disable()) 
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
