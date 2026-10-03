package org.tonylimps.unifs.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.tonylimps.unifs.model.entity.User;

import javax.crypto.SecretKey;
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

    public String generateToken(User user) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + VALID_SECONDS*1000);
        String role = (user.getServerAdmin() != null && user.getServerAdmin() == 1)
                ? "ROLE_ADMIN"
                : "ROLE_USER";
        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(KEY, Jwts.SIG.HS256)
                .compact();
    }

    public boolean isValid(String token) {
        try {
            parseToken(token);
            return true;
        } catch (SignatureException ex) {
            return false;
        }
    }
    public Claims parseToken(String token) throws SignatureException, ExpiredJwtException, MalformedJwtException, IllegalArgumentException {
        return Jwts.parser()
                .verifyWith(KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    public Claims parseWithoutVerify(String token) {
        return Jwts.parser()
                .build()
                .parseUnsecuredClaims(token)
                .getPayload();
    }
    public String getRole(Claims claims) {
        return (String) claims.get("role");
    }
    public Long getId(Claims claims) {
        return Long.parseLong(claims.getSubject());
    }
}
