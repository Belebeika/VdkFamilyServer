package com.example.vdkfamilyserver.Services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Service
public class JwtService {

    private final Set<String> blacklistedTokens = new HashSet<>();

    @Value("${jwt.secret:default-secret}")
    private String secret;

    private SecretKey secretKey;

    private final UserDetailsService userDetailsService;

    public JwtService(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @PostConstruct
    public void init() {
        if (secret == null || secret.trim().isEmpty()) {
            throw new RuntimeException("JWT secret is not configured");
        }
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    // Добавьте сохранение user ID в токен
    public String generateToken(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claim("userId", userDetails.getId()) // Сохраняем ID
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 86400000))
                .signWith(secretKey)
                .compact();
    }

    private Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // Добавьте метод для получения user ID из токена
    public Long getUserIdFromToken(String token) {
        return parseToken(token).get("userId", Long.class);
    }

    public boolean validateToken(String token) {
        if (blacklistedTokens.contains(token)) {
            return false;
        }
        try {
            parseToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public Authentication getAuthentication(String token) {
        Claims claims = parseToken(token);
        String username = claims.getSubject();

        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        if (!userDetails.isEnabled()) {
            throw new DisabledException("User account is disabled");
        }

        return new UsernamePasswordAuthenticationToken(
                userDetails,
                "",
                userDetails.getAuthorities()
        );
    }

    private static final String BEARER_PREFIX = "Bearer ";

    public String resolveToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            return authHeader.substring(BEARER_PREFIX.length());
        }
        return null;
    }

    // Метод для аннулирования токена (добавление в черный список)
    public void invalidateToken(String token) {
        blacklistedTokens.add(token);
    }
}