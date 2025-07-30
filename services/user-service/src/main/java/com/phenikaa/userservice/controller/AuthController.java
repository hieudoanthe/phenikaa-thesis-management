package com.phenikaa.userservice.controller;

import com.phenikaa.security.JwtTokenProvider;
import com.phenikaa.userservice.config.CustomUserDetails;
import com.phenikaa.userservice.dto.response.AuthResponse;
import com.phenikaa.userservice.dao.interfaces.UserDao;
import com.phenikaa.userservice.dto.request.LoginRequest;
import com.phenikaa.userservice.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserDao userDao;
    private final CustomUserDetailsService customUserDetailsService;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        String expectedRole = "ROLE_" + loginRequest.getRole();
        boolean matched = userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals(expectedRole));

        if (!matched) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Bạn không có quyền với vai trò đã chọn.");
        }

        String actualRole = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(r -> r.replace("ROLE_", ""))
                .findFirst()
                .orElse("USER");

        String accessToken = jwtTokenProvider.generateAccessToken(
                userDetails.getUsername(),
                userDetails.getUserId(),
                userDetails.getAuthorities()
        );
        String refreshToken = jwtTokenProvider.generateRefreshToken(userDetails.getUsername());

        // Log để kiểm tra
        System.out.println("userId: " + userDetails.getUserId());
        System.out.println("accessToken: " + accessToken);

        AuthResponse authResponse = new AuthResponse(userDetails.getUserId(),accessToken, refreshToken, actualRole);

        return ResponseEntity.ok(authResponse);

    }

}
