package com.phenikaa.userservice.controller;

import com.phenikaa.dto.request.LoginRequest;
import com.phenikaa.dto.request.SaveRefreshTokenRequest;
import com.phenikaa.dto.response.UserInfoResponse;
import com.phenikaa.userservice.repository.RefreshTokenRepository;
import com.phenikaa.userservice.service.interfaces.RefreshTokenService;
import com.phenikaa.userservice.service.interfaces.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/users")
public class InternalUserController {

    private final UserService userService;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/verify")
    public ResponseEntity<UserInfoResponse> verifyUser(@RequestBody LoginRequest request) {
        UserInfoResponse response = userService.verifyUser(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/saveRefreshToken")
    public ResponseEntity<Void> saveRefreshToken(@RequestBody SaveRefreshTokenRequest request) {
        refreshTokenService.save(request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/deleteRefreshToken")
    public ResponseEntity<Void> deleteRefreshToken(@RequestParam String token) {
        refreshTokenService.deleteByToken(token);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/getUserByRefreshToken")
    public ResponseEntity<UserInfoResponse> getUserByRefreshToken(@RequestParam String token) {
        UserInfoResponse response = refreshTokenService.getUserByRefreshToken(token);
        return ResponseEntity.ok(response);
    }

}
