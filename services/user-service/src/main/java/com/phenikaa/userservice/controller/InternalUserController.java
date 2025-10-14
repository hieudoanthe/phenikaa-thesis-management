package com.phenikaa.userservice.controller;

import com.phenikaa.dto.request.LoginRequest;
import com.phenikaa.dto.request.SaveRefreshTokenRequest;
import com.phenikaa.dto.response.AuthenticatedUserResponse;
import com.phenikaa.dto.response.GetUserResponse;
import com.phenikaa.userservice.service.interfaces.ImportUserService;
import com.phenikaa.userservice.service.interfaces.RefreshTokenService;
import com.phenikaa.userservice.service.interfaces.UserService;
import com.phenikaa.userservice.dto.request.ChangePasswordRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @GetMapping("/students/count/by-period/{periodId}")
    public ResponseEntity<?> getStudentCountByPeriod(@PathVariable Integer periodId) {
        try {
            log.info("Lấy tổng số sinh viên theo periodId: {}", periodId);

            List<Map<String, Object>> students = importUserService.getStudentsByPeriod(periodId);
            int totalCount = students.size();

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "periodId", periodId,
                    "totalStudents", totalCount,
                    "message", "Lấy tổng số sinh viên thành công"
            ));
        } catch (Exception e) {
            log.error("Lỗi khi lấy tổng số sinh viên theo period: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Lỗi khi lấy tổng số sinh viên: " + e.getMessage()));
        }
    }

    @GetMapping("/students/unregistered/count/by-period/{periodId}")
    public ResponseEntity<?> getUnregisteredStudentCountByPeriod(@PathVariable Integer periodId) {
        try {
            log.info("Lấy tổng số sinh viên chưa đăng ký theo periodId: {}", periodId);

            // Lấy tất cả sinh viên trong hệ thống
            List<GetUserResponse> allStudents = userService.getUsersByRole("STUDENT");
            
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "periodId", periodId,
                    "unregisteredStudents", 0, 
                    "totalStudents", allStudents.size(),
                    "registeredStudents", 0,
                    "message", "API chưa hoàn thiện - cần tích hợp với thesis-service"
            ));
        } catch (Exception e) {
            log.error("Lỗi khi lấy tổng số sinh viên chưa đăng ký theo period: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Lỗi khi lấy tổng số sinh viên chưa đăng ký: " + e.getMessage()));
        }
    }
    
    // Password reset endpoints
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, Object>> forgotPassword(@RequestParam String username) {
        log.info("Received forgot password request for username: {}", username);
        
        try {
            String token = userService.createPasswordResetToken(username);
            
            Map<String, Object> response = Map.of(
                "success", true,
                "message", "Token đã được tạo thành công",
                "token", token
            );
            
            log.info("Password reset token created successfully for username: {}", username);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error creating password reset token for username {}: {}", username, e.getMessage());
            
            Map<String, Object> response = Map.of(
                "success", false,
                "message", e.getMessage()
            );
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @GetMapping("/validate-reset-token")
    public ResponseEntity<Map<String, Object>> validateResetToken(@RequestParam String token) {
        log.info("Validating reset token");
        
        try {
            boolean isValid = userService.validatePasswordResetToken(token);
            
            Map<String, Object> response = Map.of(
                "valid", isValid,
                "message", isValid ? "Token hợp lệ" : "Token không hợp lệ hoặc đã hết hạn"
            );
            
            if (isValid) {
                Integer userId = userService.getUserIdFromToken(token);
                response = Map.of(
                    "valid", true,
                    "userId", userId,
                    "message", "Token hợp lệ"
                );
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error validating reset token: {}", e.getMessage());
            
            Map<String, Object> response = Map.of(
                "valid", false,
                "message", "Lỗi khi xác thực token"
            );
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, Object>> resetPassword(
            @RequestParam String token,
            @RequestParam String newPassword) {
        log.info("Received reset password request");
        
        try {
            boolean success = userService.resetPasswordWithToken(token, newPassword);
            
            Map<String, Object> response = Map.of(
                "success", success,
                "message", success ? "Mật khẩu đã được đặt lại thành công" : "Không thể đặt lại mật khẩu"
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error resetting password: {}", e.getMessage());
            
            Map<String, Object> response = Map.of(
                "success", false,
                "message", e.getMessage()
            );
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PutMapping("/change-password")
    public ResponseEntity<Map<String, Object>> changePassword(@RequestBody ChangePasswordRequest request) {
        try {
            userService.changePassword(request.getUserId(), request.getCurrentPassword(), request.getNewPassword());
            return ResponseEntity.ok(Map.of("success", true, "message", "Đổi mật khẩu thành công"));
        } catch (org.springframework.web.server.ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(Map.of("success", false, "message", e.getReason()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("success", false, "message", "Lỗi hệ thống"));
        }
    }
}
