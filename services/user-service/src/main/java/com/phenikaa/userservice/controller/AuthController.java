package com.phenikaa.userservice.controller;

import com.phenikaa.userservice.dto.response.AuthResponse;
import com.phenikaa.userservice.security.JwtTokenProvider;
import com.phenikaa.userservice.dao.interfaces.UserDao;
import com.phenikaa.userservice.dto.response.JwtResponse;
import com.phenikaa.userservice.dto.request.LoginRequest;
import com.phenikaa.userservice.entity.User;
import com.phenikaa.userservice.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserDao userDao;
    private final CustomUserDetailsService customUserDetailsService;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login1")
    public ResponseEntity<?> test(@RequestBody Map<String, Object> body) {
        String encodedPassword = passwordEncoder.encode("123456");
        User user = new User();
        user.setUserId("1");
        user.setUsername("admin");
        user.setFullName("Admin User");
        user.setPasswordHash(encodedPassword);
        System.out.println(encodedPassword);
        return ResponseEntity.ok("OK");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        // 1. Xác thực username + password
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        // 2. Lưu Authentication vào SecurityContextHolder
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 3. Lấy thông tin UserDetails từ DB
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(loginRequest.getUsername());

        // 4. Sinh token dùng thông tin từ UserDetails (không ép kiểu về User)
        String accessToken = jwtTokenProvider.generateAccessToken(userDetails.getUsername(), userDetails.getAuthorities());
        String refreshToken = jwtTokenProvider.generateRefreshToken(userDetails.getUsername());

        // 5. Trả về AccessToken và RefreshToken
        AuthResponse authResponse = new AuthResponse(accessToken, refreshToken);
        return ResponseEntity.ok(authResponse);
    }

}
