package nl.fontys.db3.backend.security;

import lombok.extern.slf4j.Slf4j;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@Order(1)
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public JwtAuthFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        final String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);
        
        try {
            String extractedUsername = jwtService.extractUsername(token);

            if (extractedUsername != null &&
                    SecurityContextHolder.getContext().getAuthentication() == null) {
                authenticateUser(token, extractedUsername, request);
            }
        } catch (Exception e) {
            log.warn("JWT token extraction failed - path: {}, error: {}", 
                    request.getRequestURI(), e.getMessage());
        }
        
        filterChain.doFilter(request, response);
    }

    private void authenticateUser(String token, String extractedUsername, HttpServletRequest request) {
        try {
            UserDetails userDetails = userDetailsService.loadUserByUsername(extractedUsername);
            
            boolean isValid = jwtService.isTokenValid(token, userDetails.getUsername());
            
            if (isValid) {
                log.debug("JWT authentication successful - username: {}, path: {}", 
                        extractedUsername, request.getRequestURI());
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                SecurityContextHolder.getContext().setAuthentication(authToken);
            } else {
                log.warn("JWT token validation failed - username: {}, path: {}", 
                        extractedUsername, request.getRequestURI());
            }
        } catch (UsernameNotFoundException e) {
            log.warn("JWT authentication failed - user not found: {}, path: {}", 
                    extractedUsername, request.getRequestURI());
        } catch (Exception e) {
            log.error("JWT authentication error - username: {}, path: {}", 
                    extractedUsername, request.getRequestURI(), e);
        }
    }
}