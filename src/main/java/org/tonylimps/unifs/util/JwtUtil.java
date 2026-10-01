package org.tonylimps.unifs.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.util.Date;

@Component
public class JwtUtil {
    private final SecretKey KEY;
    private final long VALID_SECONDS;

    public JwtUtil(@Value("${jwt.secret-key}") String key,
                   @Value("${jwt.valid-seconds}") long valid) {
        KEY = Keys.hmacShaKeyFor(key.getBytes());
        VALID_SECONDS = valid;
    }

    public String generateToken(Long userId) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + VALID_SECONDS*1000);
        return Jwts.builder()
                .subject(userId.toString())
                .issuedAt(now)
                .expiration(expiration)
                .signWith(KEY, Jwts.SIG.HS256)
                .compact();
    }

    public Claims parseToken(String token) throws SignatureException {
        return Jwts.parser()
                .verifyWith(KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
