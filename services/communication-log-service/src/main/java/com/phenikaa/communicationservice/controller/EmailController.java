package com.phenikaa.communicationservice.controller;

import com.phenikaa.communicationservice.dto.request.PeriodEmailRequest;
import com.phenikaa.communicationservice.dto.response.EmailResponse;
import com.phenikaa.communicationservice.service.implement.EmailServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/api/communication-service/admin")
@Slf4j
public class EmailController {

    @Autowired
    private EmailServiceImpl emailServiceImpl;

    /**
     * Gửi email cho sinh viên trong đợt đăng ký
     */
    @PostMapping("/send-period-email")
    public Mono<ResponseEntity<EmailResponse>> sendPeriodEmail(@RequestBody PeriodEmailRequest request) {
        log.info("Received request to send period email: periodId={}, periodName={}", 
                request.getPeriodId(), request.getPeriodName());
        
        // Set default values if not provided
        if (request.getTargetDomain() == null || request.getTargetDomain().isEmpty()) {
            request.setTargetDomain("@st.phenikaa-uni.edu.vn");
        }
        
        if (request.getSubject() == null || request.getSubject().isEmpty()) {
            request.setSubject("[THÔNG BÁO] Mở đợt đăng ký khóa luận tốt nghiệp");
        }
        
        if (request.getStatus() == null || request.getStatus().isEmpty()) {
            request.setStatus("Đang hoạt động");
        }
        
		if (request.getSystemUrl() == null || request.getSystemUrl().isEmpty()
				|| request.getSystemUrl().contains("phenikka-uni.edu.vn")
				|| request.getSystemUrl().contains("thesis.")) {
			request.setSystemUrl("https://phenikaa-thesis-management-fe.vercel.app/");
		}
        
        if (request.getSupportEmail() == null || request.getSupportEmail().isEmpty()) {
            request.setSupportEmail("support@phenikka-uni.edu.vn");
        }
        
        if (request.getSupportPhone() == null || request.getSupportPhone().isEmpty()) {
            request.setSupportPhone("024.1234.5678");
        }

        return emailServiceImpl.sendEmailToPeriodStudents(request)
                .map(response -> {
                    if (response.isSuccess()) {
                        log.info("Email sending completed successfully. Sent: {}, Failed: {}", 
                                response.getTotalSent(), response.getTotalFailed());
                        return ResponseEntity.ok(response);
                    } else {
                        log.warn("Email sending completed with errors. Sent: {}, Failed: {}", 
                                response.getTotalSent(), response.getTotalFailed());
                        return ResponseEntity.ok(response); // Vẫn trả về 200 vì có thể một số email đã gửi thành công
                    }
                })
                .onErrorResume(throwable -> {
                    log.error("Error in sendPeriodEmail: {}", throwable.getMessage(), throwable);
                    EmailResponse errorResponse = new EmailResponse(
                            false, 
                            "Lỗi hệ thống: " + throwable.getMessage(), 
                            0, 0, 
                            null, null
                    );
                    return Mono.just(ResponseEntity.internalServerError().body(errorResponse));
                });
    }

    /**
     * API test gửi email
     */
    @PostMapping("/test-email")
    public ResponseEntity<String> testEmail(@RequestParam String email) {
        log.info("Testing email to: {}", email);
        
        try {
            PeriodEmailRequest testRequest = new PeriodEmailRequest();
            testRequest.setPeriodId(1L);
            testRequest.setPeriodName("Test Period");
            testRequest.setSubject("Test Email");
            testRequest.setMessage("This is a test email from the system.");
            testRequest.setTargetDomain("@st.phenikaa-uni.edu.vn");
            
            EmailResponse response = emailServiceImpl.sendEmailToPeriodStudents(testRequest).block();
            
            if (response.isSuccess()) {
                return ResponseEntity.ok("Test email sent successfully");
            } else {
                return ResponseEntity.badRequest().body("Failed to send test email: " + response.getMessage());
            }
            
        } catch (Exception e) {
            log.error("Error in test email: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }
    
    /**
     * Gửi email reset password
     */
    @PostMapping("/send-password-reset-email")
    public ResponseEntity<Map<String, Object>> sendPasswordResetEmail(@RequestBody PasswordResetEmailRequest request) {
        log.info("Sending password reset email to: {}", request.getEmail());
        
        try {
            emailServiceImpl.sendPasswordResetEmail(request.getEmail(), request.getToken(), request.getResetUrl());
            
            Map<String, Object> response = Map.of(
                "success", true,
                "message", "Email đặt lại mật khẩu đã được gửi thành công"
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error sending password reset email: {}", e.getMessage(), e);
            
            Map<String, Object> response = Map.of(
                "success", false,
                "message", "Không thể gửi email: " + e.getMessage()
            );
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    // Request DTO cho password reset email
    public static class PasswordResetEmailRequest {
        private String email;
        private String token;
        private String resetUrl;
        
        public PasswordResetEmailRequest() {}
        
        public PasswordResetEmailRequest(String email, String token, String resetUrl) {
            this.email = email;
            this.token = token;
            this.resetUrl = resetUrl;
        }
        
        public String getEmail() {
            return email;
        }
        
        public void setEmail(String email) {
            this.email = email;
        }
        
        public String getToken() {
            return token;
        }
        
        public void setToken(String token) {
            this.token = token;
        }
        
        public String getResetUrl() {
            return resetUrl;
        }
        
        public void setResetUrl(String resetUrl) {
            this.resetUrl = resetUrl;
        }
    }
}
