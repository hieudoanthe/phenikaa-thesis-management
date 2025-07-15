package com.phenikaa.userservice.security;

import com.phenikaa.userservice.entity.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpirationInMs;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpirationMs;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());  // Sử dụng secret key để tạo Signing Key
    }

    // Sinh Access Token
    public String generateAccessToken(String username, Collection<? extends GrantedAuthority> authorities) {
        List<String> roles = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        return Jwts.builder()
                .setSubject(username)
                .claim("roles", roles)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    // Sinh Refresh Token
    public String generateRefreshToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshExpirationMs);  // Lấy thời gian hết hạn cho refresh token

        return Jwts.builder()
                .setSubject(username)  // Đặt username là subject
                .setIssuedAt(now)  // Thời gian tạo token
                .setExpiration(expiryDate)  // Thời gian hết hạn
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)  // Sử dụng HS512 để ký refresh token
                .compact();
    }

    // Lấy username từ JWT
    public String getUsernameFromJWT(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())  // Đặt signing key
                .build()
                .parseClaimsJws(token)  // Parse claims từ token
                .getBody()
                .getSubject();  // Lấy username từ subject
    }

    // Kiểm tra tính hợp lệ của token
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token);
            return true;
        } catch (Exception ex) {
            System.out.println("Token không hợp lệ: " + ex.getMessage());
            return false;
        }
    }

    // Lấy thời gian hết hạn của token (để debug hoặc log)
    public Date getExpirationDateFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())  // Đặt signing key
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getExpiration();  // Lấy thời gian hết hạn
    }
}
