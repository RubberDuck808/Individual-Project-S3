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
            // Enable CORS so your CorsConfigurationSource bean is applied
            .cors(Customizer.withDefaults())
            // Disable CSRF for APIs (adjust if you later use sessions/cookies)
            .csrf(AbstractHttpConfigurer::disable)
            // Authorize requests
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/users/**").permitAll()
                .requestMatchers("/api/votes/**").permitAll()
                .requestMatchers("/api/hazards/**").permitAll()
                .requestMatchers("/api/hazard-categories/**").permitAll()
                .anyRequest().permitAll() // adjust to .authenticated() later if needed
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
