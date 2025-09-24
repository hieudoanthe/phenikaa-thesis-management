package com.phenikaa.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Base64;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-expiration}")
    private long refreshTokenExpiration;

    private Key signingKey;

    @jakarta.annotation.PostConstruct
    public void init() {
        signingKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(jwtSecret));
    }

    private Key getSigningKey() {
        return signingKey;
    }

    private String cleanToken(String token) {
        return token.replace("Bearer ", "").trim();
    }

    public String generateAccessToken(String username, Integer userId, Collection<? extends GrantedAuthority> authorities) {
        List<String> roles = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenExpiration);

        return Jwts.builder()
                .setSubject(username)
                .claim("roles", roles)
                .claim("userId", userId)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    public String generateRefreshToken(String username, Integer userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshTokenExpiration);

        return Jwts.builder()
                .setSubject(username)
                .claim("userId", userId)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    public Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(cleanToken(token))
                .getBody();
    }

    public Integer extractUserId(String token) {
        return extractClaims(token).get("userId", Integer.class);
    }

    public String getUsernameFromJWT(String token) {
        return extractClaims(token).getSubject();
    }

    public List<String> getRoles(String token) {
        return extractClaims(token).get("roles", List.class);
    }

    public boolean validateToken(String token) {
        try {
            extractClaims(token);
            return true;
        } catch (Exception ex) {
            System.out.println("Token invalid: " + ex.getMessage());
            return false;
        }
    }

    public Date getExpirationDateFromToken(String token) {
        return extractClaims(token).getExpiration();
    }

    /**
     * Generate JWT token for internal service calls
     */
    public String generateInternalServiceToken() {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenExpiration);

        return Jwts.builder()
                .setSubject("internal-service")
                .claim("roles", List.of("ROLE_ADMIN"))
                .claim("userId", 0)
                .claim("service", "user-service")
                .claim("type", "internal")
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

}
