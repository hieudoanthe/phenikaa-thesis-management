package com.phenikaa.authservice.controller;

import com.phenikaa.authservice.client.UserServiceClient;
import com.phenikaa.authservice.client.CommunicationServiceClient;
import com.phenikaa.dto.request.RefreshTokenRequest;
import com.phenikaa.dto.request.LoginRequest;
import com.phenikaa.authservice.dto.response.AuthResponse;
import com.phenikaa.authservice.service.implement.AuthService;
import com.phenikaa.dto.response.RefreshTokenResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import com.phenikaa.utils.JwtUtil;
import java.util.HashMap;

import java.util.Map;

@RestController
@RequestMapping("api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final UserServiceClient userServiceClient;
    private final CommunicationServiceClient communicationServiceClient;
    private final JwtUtil jwtUtil;
    
    @Value("${app.frontend.url}")
    private String frontendUrl;

    @PostMapping("/login")
    public Mono<ResponseEntity<AuthResponse>> login(@RequestBody LoginRequest request) {
        return authService.login(request)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> {
                    log.error("Login failed: {}", e.getMessage());
                    return Mono.just(ResponseEntity.status(401).build());
                });
    }

    @PostMapping("/logout")
    public Mono<ResponseEntity<Void>> logout(@RequestBody RefreshTokenResponse request) {
        return userServiceClient.deleteRefreshToken(request.getRefreshToken())
                .thenReturn(ResponseEntity.ok().<Void>build());
    }

    @PostMapping("/refresh-token")
    public Mono<ResponseEntity<AuthResponse>> refreshAccessToken(@RequestBody RefreshTokenRequest request) {
        return authService.refreshAccessToken(request)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()));
    }

    @PostMapping("/forgot-password")
    public Mono<ResponseEntity<Map<String, Object>>> forgotPassword(@RequestParam String email) {
        log.info("Received forgot password request for email: {}", email);
        
        return userServiceClient.createPasswordResetToken(email)
                .flatMap(tokenResponse -> {
                    if (!(Boolean) tokenResponse.get("success")) {
                        Map<String, Object> response = Map.of(
                            "success", false,
                            "message", tokenResponse.get("message")
                        );
                        return Mono.just(ResponseEntity.badRequest().body(response));
                    }
                    
                    String token = (String) tokenResponse.get("token");
                    String resetUrl = frontendUrl + "/reset-password?token=" + token;
                    
                    return communicationServiceClient.sendPasswordResetEmail(email, token, resetUrl)
                            .map(emailResponse -> {
                                Map<String, Object> response;
                                if ((Boolean) emailResponse.get("success")) {
                                    response = Map.of(
                                        "success", true,
                                        "message", "Email đặt lại mật khẩu đã được gửi đến " + email
                                    );
                                } else {
                                    response = Map.of(
                                        "success", false,
                                        "message", "Không thể gửi email. Vui lòng thử lại sau."
                                    );
                                }
                                return ResponseEntity.ok(response);
                            });
                })
                .onErrorResume(e -> {
                    log.error("Error in forgot password for email {}: {}", email, e.getMessage());
                    Map<String, Object> response = Map.of(
                        "success", false,
                        "message", "Có lỗi xảy ra. Vui lòng thử lại sau."
                    );
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response));
                });
    }

    @PostMapping("/reset-password")
    public Mono<ResponseEntity<Map<String, Object>>> resetPassword(
            @RequestParam String token,
            @RequestParam String newPassword) {
        log.info("Received reset password request");
        // Lấy userId từ token để set senderId cho thông báo
        return userServiceClient.validateResetToken(token)
                .flatMap(validateResp -> {
                    Integer senderId = null;
                    try {
                        Object uid = validateResp.get("userId");
                        if (uid instanceof Integer) senderId = (Integer) uid;
                        else if (uid instanceof Number) senderId = ((Number) uid).intValue();
                    } catch (Exception ignored) {}

                    final Integer finalSenderId = senderId;
                    return userServiceClient.resetPassword(token, newPassword)
                            .flatMap(resp -> {
                                Map<String, Object> payload = new HashMap<>();
                                if (finalSenderId != null) payload.put("senderId", finalSenderId);
                                payload.put("receiverId", 1);
                                payload.put("type", "PASSWORD_RESET");
                                payload.put("title", "Đặt lại mật khẩu");
                                String msg = (finalSenderId != null)
                                        ? ("Người dùng với ID: " + finalSenderId + " vừa thực hiện đặt lại mật khẩu!")
                                        : "Một người dùng vừa đặt lại mật khẩu thành công.";
                                payload.put("message", msg);
                                return communicationServiceClient.sendNotification(payload)
                                        .onErrorResume(e -> Mono.empty())
                                        .thenReturn(ResponseEntity.ok(resp));
                            });
                })
                .onErrorResume(e -> {
                    log.error("Error in reset password: {}", e.getMessage());
                    Map<String, Object> response = Map.of(
                        "success", false,
                        "message", "Có lỗi xảy ra. Vui lòng thử lại sau."
                    );
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response));
                });
    }

    @GetMapping("/validate-reset-token")
    public Mono<ResponseEntity<Map<String, Object>>> validateResetToken(@RequestParam String token) {
        log.info("Validating reset token");
        
        return userServiceClient.validateResetToken(token)
                .map(response -> ResponseEntity.ok(response))
                .onErrorResume(e -> {
                    log.error("Error validating reset token: {}", e.getMessage());
                    Map<String, Object> response = Map.of(
                        "valid", false,
                        "message", "Có lỗi xảy ra khi xác thực token."
                    );
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response));
                });
    }

    @PutMapping("/change-password")
    public Mono<ResponseEntity<Map<String, Object>>> changePassword(
            @RequestHeader("Authorization") String authorization,
            @RequestBody Map<String, String> body) {
        try {
            String accessToken = authorization != null ? authorization.replace("Bearer ", "").trim() : null;
            Integer userId = jwtUtil.extractUserId(accessToken);
            String currentPassword = body.get("currentPassword");
            String newPassword = body.get("newPassword");

            return userServiceClient.changePassword(userId, currentPassword, newPassword)
                    .flatMap(resp -> {
                        Map<String, Object> payload = new HashMap<>();
                        payload.put("senderId", userId);
                        payload.put("receiverId", 1);
                        payload.put("type", "PASSWORD_CHANGED");
                        payload.put("title", "Đổi mật khẩu");
                        payload.put("message", "User ID " + userId + " vừa đổi mật khẩu.");
                        return communicationServiceClient.sendNotification(payload)
                                .onErrorResume(e -> Mono.empty())
                                .thenReturn(ResponseEntity.ok(resp));
                    })
                    .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(java.util.Map.of("success", false, "message", "Không thể đổi mật khẩu"))));
        } catch (Exception e) {
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(java.util.Map.of("success", false, "message", "Token không hợp lệ")));
        }
    }

    // Dành cho thay đổi mật khẩu trực tiếp tại màn login (chưa đăng nhập): cần email + currentPassword
    @PutMapping("/change-password-direct")
    public Mono<ResponseEntity<Map<String, Object>>> changePasswordDirect(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String currentPassword = body.get("currentPassword");
        String newPassword = body.get("newPassword");

        if (email == null || currentPassword == null || newPassword == null) {
            return Mono.just(ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Thiếu thông tin bắt buộc"
            )));
        }

        LoginRequest verifyReq = new LoginRequest();
        verifyReq.setUsername(email);
        verifyReq.setPassword(currentPassword);

        return userServiceClient.verifyUser(verifyReq)
                .flatMap(authUser -> userServiceClient.changePassword(authUser.id(), currentPassword, newPassword)
                        .flatMap(resp -> {
                            Map<String, Object> payload = new HashMap<>();
                            payload.put("senderId", authUser.id());
                            payload.put("receiverId", 1);
                            payload.put("type", "PASSWORD_CHANGED");
                            payload.put("title", "Đổi mật khẩu");
                            payload.put("message", "Người dùng " + email + " vừa đổi mật khẩu.");
                            return communicationServiceClient.sendNotification(payload)
                                    .onErrorResume(e -> Mono.empty())
                                    .thenReturn(ResponseEntity.ok(resp));
                        }))
                .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new HashMap<String, Object>() {{
                            put("success", false);
                            put("message", "Email hoặc mật khẩu hiện tại không đúng!");
                        }})));
    }

}
