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

    @Value("${app.jwt.expiration-ms:86400000}") // 1 day
    private long expirationMs;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String extractUsername(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())     // now correct type
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
                .signWith(getSigningKey())       // correct — expects SecretKey
                .compact();
    }

    public boolean isTokenValid(String token, String username) {
        try {
            String tokenUsername = extractUsername(token);

            Date expiration = Jwts.parser()
                    .verifyWith(getSigningKey()) // correct type
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getExpiration();

            return tokenUsername.equals(username)
                    && expiration.after(new Date());

        } catch (Exception e) {
            return false;
        }
    }
}
