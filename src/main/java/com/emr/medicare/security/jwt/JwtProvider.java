package com.emr.medicare.security.jwt;

import com.emr.medicare.user.entity.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtProvider {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-expiration}")
    private long accessExpiration;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        secretKey = Keys.hmacShaKeyFor(
                secret.getBytes(StandardCharsets.UTF_8)
        );
    }

    public String createAccessToken(
            Long userId,
            String email,
            String name,
            Role role
    ) {
        return createToken(
                userId,
                email,
                name,
                role,
                accessExpiration
        );
    }

    public String createRefreshToken(
            Long userId,
            String email,
            String name,
            Role role
    ) {
        return createToken(
                userId,
                email,
                name,
                role,
                refreshExpiration
        );
    }

    private String createToken(
            Long userId,
            String email,
            String name,
            Role role,
            long expiration
    ) {

        Date now = new Date();
        Date expiry = new Date(now.getTime() + expiration);

        String safeName = name == null ? "" : name;

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("email", email)
                .claim("name", safeName)
                .claim("role", role.name())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();
    }

    public boolean validateToken(String token) {

        try {
            parseClaims(token);
            return true;

        } catch (Exception e) {
            return false;
        }
    }

    public Claims parseClaims(String token) {

        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Long getUserId(String token) {

        return Long.valueOf(
                parseClaims(token).getSubject()
        );
    }

    public long getRemainingTime(String token) {

        Date expiration =
                parseClaims(token).getExpiration();

        return expiration.getTime()
                - System.currentTimeMillis();
    }
}