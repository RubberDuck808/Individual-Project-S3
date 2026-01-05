package nl.fontys.db3.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // System.out.println("=== JWT FILTER START ===");
        final String header = request.getHeader("Authorization");
        // System.out.println("Authorization Header: " + header);

        if (header == null || !header.startsWith("Bearer ")) {
            // System.out.println("No Bearer token, skipping filter");
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);
        // System.out.println("Token: " + token);
        
        String extractedUsername = jwtService.extractUsername(token);
        // System.out.println("Extracted username from token: " + extractedUsername);

        if (extractedUsername != null &&
                SecurityContextHolder.getContext().getAuthentication() == null) {

            // System.out.println("Loading UserDetails for: " + extractedUsername);
            try {
                UserDetails userDetails = userDetailsService.loadUserByUsername(extractedUsername);
                // System.out.println("Loaded UserDetails username: " + userDetails.getUsername());
                // System.out.println("UserDetails authorities: " + userDetails.getAuthorities());
                
                // Debug: Check both values
                // System.out.println("Comparing: tokenUsername='" + extractedUsername + 
                //                  "' with userDetailsUsername='" + userDetails.getUsername() + "'");
                
                boolean isValid = jwtService.isTokenValid(token, userDetails.getUsername());
                // System.out.println("Token validation result: " + isValid);
                
                if (isValid) {
                    // System.out.println("Token is valid, setting authentication");
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
                    // System.out.println("Authentication set in SecurityContext");
                } else {
                    // System.out.println("Token is INVALID!");
                }
            } catch (UsernameNotFoundException e) {
                // System.out.println("User not found: " + e.getMessage());
            }
        } else {
            // System.out.println("Username is null or already authenticated");
        }

        // System.out.println("=== JWT FILTER END ===");
        filterChain.doFilter(request, response);
    }
}