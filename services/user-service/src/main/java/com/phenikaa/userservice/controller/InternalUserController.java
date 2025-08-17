package com.phenikaa.userservice.controller;

import com.phenikaa.dto.request.LoginRequest;
import com.phenikaa.dto.request.SaveRefreshTokenRequest;
import com.phenikaa.dto.response.AuthenticatedUserResponse;
import com.phenikaa.dto.response.GetUserResponse;
import com.phenikaa.userservice.service.interfaces.RefreshTokenService;
import com.phenikaa.userservice.service.interfaces.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/users")
public class InternalUserController {

    private final UserService userService;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/verify")
    public ResponseEntity<AuthenticatedUserResponse> verifyUser(@RequestBody LoginRequest request) {
        AuthenticatedUserResponse response = userService.verifyUser(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/save-refresh-token")
    public ResponseEntity<Void> saveRefreshToken(@RequestBody SaveRefreshTokenRequest request) {
        refreshTokenService.save(request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/delete-refresh-token")
    public ResponseEntity<Void> deleteRefreshToken(@RequestParam String token) {
        refreshTokenService.deleteByToken(token);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/get-use-by-refreshToken")
    public ResponseEntity<AuthenticatedUserResponse> getUserByRefreshToken(@RequestParam String token) {
        AuthenticatedUserResponse response = refreshTokenService.getUserByRefreshToken(token);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/get-profile/{userId}")
    public ResponseEntity<GetUserResponse> getUser(@PathVariable Integer userId) {
        return ResponseEntity.ok(userService.getUserById(userId));
    }

}
