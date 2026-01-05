package nl.fontys.db3.backend.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.expiration-ms:86400000}")
    private long expirationMs;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String extractUsername(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey()) 
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public String generateToken(String username, Map<String, Object> extraClaims) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .claims(extraClaims)
                .subject(username)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(getSigningKey())
                .compact();
    }

    public boolean isTokenValid(String token, String username) {
        try {
            String tokenUsername = extractUsername(token);
            // System.out.println("[JwtService] Token username: " + tokenUsername);
            // System.out.println("[JwtService] Provided username: " + username);

            Date expiration = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getExpiration();
            
            // System.out.println("[JwtService] Token expires: " + expiration);
            // System.out.println("[JwtService] Current time: " + new Date());
            
            boolean usernameValid = tokenUsername.equals(username);
            boolean notExpired = expiration.after(new Date());
            
            // System.out.println("[JwtService] Username valid: " + usernameValid);
            // System.out.println("[JwtService] Not expired: " + notExpired);

            return usernameValid && notExpired;

        } catch (Exception e) {
            // System.out.println("[JwtService] Token validation error: " + e.getMessage());
            // e.printStackTrace();
            return false;
        }
    }
}
