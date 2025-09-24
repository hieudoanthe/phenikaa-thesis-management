package com.phenikaa.userservice.controller;

import com.phenikaa.dto.request.LoginRequest;
import com.phenikaa.dto.request.SaveRefreshTokenRequest;
import com.phenikaa.dto.response.AuthenticatedUserResponse;
import com.phenikaa.dto.response.GetUserResponse;
import com.phenikaa.userservice.service.interfaces.ImportUserService;
import com.phenikaa.userservice.service.interfaces.RefreshTokenService;
import com.phenikaa.userservice.service.interfaces.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/internal/users")
public class InternalUserController {

    private final UserService userService;
    private final RefreshTokenService refreshTokenService;
    private final ImportUserService importUserService;

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

    @PostMapping("/get-all-users/batch")
    public List<GetUserResponse> getUsersByIds(@RequestBody List<Integer> userIds) {
        return userService.getUserByIds(userIds);
    }

    @GetMapping("/get-username/{userId}")
    public ResponseEntity<String> getUsername(@PathVariable Integer userId) {
        GetUserResponse user = userService.getUserById(userId);
        return ResponseEntity.ok(user.getUsername());
    }

    // New: expose users by role for internal services (e.g., notifications)
    @GetMapping("/by-role")
    public ResponseEntity<List<GetUserResponse>> getUsersByRole(@RequestParam String role) {
        return ResponseEntity.ok(userService.getUsersByRole(role));
    }

    @GetMapping("/students/by-period/{periodId}")
    public ResponseEntity<?> getStudentsByPeriod(@PathVariable Integer periodId) {
        try {
            log.info("Lấy danh sách sinh viên theo periodId: {}", periodId);

            List<Map<String, Object>> students = importUserService.getStudentsByPeriod(periodId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", students,
                    "message", "Lấy danh sách sinh viên thành công"
            ));
        } catch (Exception e) {
            log.error("Lỗi khi lấy danh sách sinh viên theo period: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Lỗi khi lấy danh sách sinh viên: " + e.getMessage()));
        }
    }
}
