package com.phenikaa.communicationservice.service.implement;

import com.phenikaa.communicationservice.client.UserServiceClient;
import com.phenikaa.communicationservice.dto.request.PeriodEmailRequest;
import com.phenikaa.communicationservice.dto.response.EmailResponse;
import jakarta.mail.internet.MimeMessage;
import com.phenikaa.communicationservice.client.PeriodServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.annotation.PreDestroy;
import reactor.core.publisher.Mono;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class EmailServiceImpl {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private UserServiceClient userServiceClient;

    @Autowired
    private PeriodServiceClient periodServiceClient;

    // Thread pool cho gửi email với concurrency cao
    private final ExecutorService emailExecutor = Executors.newFixedThreadPool(20);

    /**
     * Gửi email cho tất cả sinh viên trong đợt đăng ký
     */
    public Mono<EmailResponse> sendEmailToPeriodStudents(PeriodEmailRequest request) {
        log.info("Starting to send emails for period: {} - {}", request.getPeriodId(), request.getPeriodName());
        
        Mono<PeriodServiceClient.PeriodTimes> periodTimesMono = Mono.justOrEmpty(request.getStartDate()).zipWith(Mono.justOrEmpty(request.getEndDate()), (s, e) -> new PeriodServiceClient.PeriodTimes(s, e, request.getStatus(), request.getPeriodName()))
                .switchIfEmpty(periodServiceClient.getPeriodTimes(request.getPeriodId()));

        return periodTimesMono.defaultIfEmpty(new PeriodServiceClient.PeriodTimes(null, null, request.getStatus(), null))
                .flatMap(periodTimes -> {
                    if (request.getStartDate() == null) request.setStartDate(periodTimes.startDate);
                    if (request.getEndDate() == null) request.setEndDate(periodTimes.endDate);
                    if (request.getStatus() == null) request.setStatus(periodTimes.status);
                    if (request.getPeriodName() == null || request.getPeriodName().isBlank()) request.setPeriodName(periodTimes.name);

                    log.info("Period info resolved - id: {}, name: {}, start: {}, end: {}, status: {}",
                            request.getPeriodId(),
                            request.getPeriodName(),
                            request.getStartDate(),
                            request.getEndDate(),
                            request.getStatus());

                    return userServiceClient.getStudentsByPeriod(request.getPeriodId())
                .collectList()
                .flatMap(usernames -> {
                    if (usernames.isEmpty()) {
                        log.warn("No students found for period: {}", request.getPeriodId());
                        return Mono.just(new EmailResponse(
                                true, 
                                "Không có sinh viên nào trong đợt này", 
                                0, 0, 
                                new ArrayList<>(), 
                                new ArrayList<>()
                        ));
                    }

                    // Log raw usernames for debugging
                    log.info("Fetched {} usernames for period {}: {}", usernames.size(), request.getPeriodId(), usernames);

                    // Chuẩn hóa: nếu username chưa có domain thì nối domain mặc định
                    String tmpDomain = request.getTargetDomain();
                    final String requiredDomain = (tmpDomain == null || tmpDomain.isEmpty())
                            ? "@st.phenikaa-uni.edu.vn"
                            : tmpDomain;

                    List<String> normalizedEmails = usernames.stream()
                            .filter(u -> u != null && !u.isBlank())
                            .map(String::trim)
                            .map(username -> username.contains("@") ? username : username + requiredDomain)
                            .filter(email -> email.matches("^[^@\n\r\t ]+@[^@\n\r\t ]+$"))
                            .toList();

                    // Dùng tất cả email hợp lệ (username có thể đã là email đầy đủ với domain khác)
                    List<String> validStudents = normalizedEmails;

                    if (validStudents.isEmpty()) {
                        log.warn("No students with domain {} found for period: {}", 
                                requiredDomain, request.getPeriodId());
                        return Mono.just(new EmailResponse(
                                true, 
                                "Không có sinh viên nào có email hợp lệ", 
                                0, 0, 
                                new ArrayList<>(), 
                                new ArrayList<>()
                        ));
                    }

                    log.info("Found {} students with valid email domain", validStudents.size());
                    
                    // Gửi email bất đồng bộ cho tất cả sinh viên
                    return sendBulkEmailsReactive(validStudents, request);
                });
                })
                .onErrorResume(throwable -> {
                    log.error("Error sending emails for period: {}", throwable.getMessage(), throwable);
                    return Mono.just(new EmailResponse(
                            false, 
                            "Lỗi khi gửi email: " + throwable.getMessage(), 
                            0, 0, 
                            new ArrayList<>(), 
                            new ArrayList<>()
                    ));
                });
    }

    /**
     * Gửi email hàng loạt (reactive version với concurrency cao)
     */
    private Mono<EmailResponse> sendBulkEmailsReactive(List<String> studentEmails, PeriodEmailRequest request) {
        // Sử dụng CompletableFuture với thread pool tùy chỉnh để tối ưu hiệu suất
        return Mono.fromCallable(() -> {
            List<CompletableFuture<EmailResult>> futures = studentEmails.stream()
                    .map(email -> CompletableFuture.supplyAsync(() -> {
                        try {
                            sendSingleEmail(email, request);
                            log.info("Email sent successfully to: {}", email);
                            return new EmailResult(email, true, null);
                        } catch (Exception e) {
                            log.error("Failed to send email to {}: {}", email, e.getMessage());
                            return new EmailResult(email, false, e.getMessage());
                        }
                    }, emailExecutor))
                    .toList();

            // Đợi tất cả futures hoàn thành với timeout
            CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                    futures.toArray(new CompletableFuture[0])
            );

            try {
                allFutures.get(5, TimeUnit.MINUTES); // Timeout 5 phút
            } catch (Exception e) {
                log.error("Timeout or error waiting for email futures: {}", e.getMessage());
            }

            // Thu thập kết quả
            List<String> successEmails = new ArrayList<>();
            List<String> failedEmails = new ArrayList<>();

            for (CompletableFuture<EmailResult> future : futures) {
                try {
                    EmailResult result = future.get();
                    if (result.isSuccess()) {
                        successEmails.add(result.getEmail());
                    } else {
                        failedEmails.add(result.getEmail());
                    }
                } catch (Exception e) {
                    log.error("Error getting email result: {}", e.getMessage());
                }
            }

            log.info("Email sending completed. Success: {}, Failed: {}", 
                    successEmails.size(), failedEmails.size());

            return new EmailResponse(
                    failedEmails.isEmpty(),
                    String.format("Gửi email hoàn tất. Thành công: %d, Thất bại: %d", 
                            successEmails.size(), failedEmails.size()),
                    successEmails.size(),
                    failedEmails.size(),
                    failedEmails,
                    successEmails
            );
        })
        .subscribeOn(reactor.core.scheduler.Schedulers.boundedElastic());
    }


    /**
     * Gửi email cho một sinh viên
     */
    private void sendSingleEmail(String studentEmail, PeriodEmailRequest request) throws Exception {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(studentEmail);
        helper.setSubject(request.getSubject() != null ? request.getSubject() : 
                "[THÔNG BÁO] Mở đợt đăng ký khóa luận tốt nghiệp");
        helper.setText(buildEmailContent(studentEmail, request), true);

        mailSender.send(message);
    }

    /**
     * Xây dựng nội dung email
     */
    private String buildEmailContent(String studentEmail, PeriodEmailRequest request) {
        // Lấy tên sinh viên từ email (phần trước @)
        String studentName = studentEmail.split("@")[0];
        
        // Format ngày tháng
        String startDateStr = request.getStartDate() != null ? 
                request.getStartDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "Chưa xác định";
        String endDateStr = request.getEndDate() != null ? 
                request.getEndDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "Chưa xác định";
        
        // Set default values
        String status = request.getStatus() != null ? request.getStatus() : "Đang hoạt động";
        String systemUrl = request.getSystemUrl() != null ? request.getSystemUrl() : "https://phenikaa-thesis-management-fe.vercel.app/";
        if (systemUrl.contains("phenikka-uni.edu.vn") || systemUrl.contains("thesis.")) {
            systemUrl = "https://phenikaa-thesis-management-fe.vercel.app/";
        }
        String supportEmail = request.getSupportEmail() != null ? request.getSupportEmail() : "support@phenikaa-uni.edu.vn";
        String supportPhone = request.getSupportPhone() != null ? request.getSupportPhone() : "024.1234.5678";

        return String.format("""
            <html>
            <body style="font-family:Arial,Helvetica,sans-serif; color:#111827; margin:0; padding:20px; background-color:#f9fafb;">
                <div style="max-width:640px;margin:0 auto;border:1px solid #e5e7eb;border-radius:12px;overflow:hidden;background:#ffffff;box-shadow:0 4px 6px -1px rgba(0,0,0,0.1)">
                    <!-- Header -->
                    <div style="background:#111827;color:#fff;padding:20px;text-align:center">
                        <h1 style="margin:0;font-size:20px;font-weight:600">[THÔNG BÁO] Mở đợt đăng ký khóa luận tốt nghiệp</h1>
                    </div>
                    
                    <!-- Content -->
                    <div style="padding:30px">
                        <p style="margin:0 0 20px;font-size:16px;line-height:1.6">
                            Xin chào <strong>%s</strong>,
                        </p>
                        
                        <p style="margin:0 0 20px;font-size:16px;line-height:1.6">
                            Bạn đã được thêm vào đợt đăng ký đề tài khóa luận:
                        </p>
                        
                        <!-- Period Info -->
                        <div style="background:#f8fafc;border:1px solid #e2e8f0;border-radius:8px;padding:20px;margin:20px 0">
                            <p style="margin:0 0 10px;font-size:16px;font-weight:600;color:#111827">
                                <strong>Tên đợt:</strong> %s
                            </p>
                            <p style="margin:0 0 10px;font-size:16px;color:#4b5563">
                                <strong>Thời gian:</strong> %s - %s
                            </p>
                            <p style="margin:0;font-size:16px;color:#4b5563">
                                <strong>Trạng thái:</strong> <span style="color:#059669;font-weight:600">%s</span>
                            </p>
                        </div>
                        
                        <!-- Login Info -->
                        <div style="background:#fef3c7;border:1px solid #f59e0b;border-radius:8px;padding:20px;margin:20px 0">
                            <h3 style="margin:0 0 15px;font-size:16px;color:#92400e;font-weight:600">Thông tin tài khoản đăng nhập hệ thống:</h3>
                            <p style="margin:0 0 8px;font-size:15px;color:#92400e">
                                <strong>Email:</strong> %s
                            </p>
                            <p style="margin:0;font-size:15px;color:#92400e">
                                <strong>Mật khẩu mặc định:</strong> <code style="background:#fbbf24;padding:2px 6px;border-radius:4px;font-family:monospace">123456PKA@</code>
                            </p>
                        </div>
                        
                        <!-- Instructions -->
                        <div style="background:#ecfdf5;border:1px solid #10b981;border-radius:8px;padding:20px;margin:20px 0">
                            <p style="margin:0 0 15px;font-size:16px;color:#065f46;font-weight:600">
                                Vui lòng truy cập hệ thống để thay đổi mật khẩu và đăng ký đề tài trong thời gian quy định.
                            </p>
                            <p style="margin:0 0 8px;font-size:15px;color:#065f46">
                                <strong>Link đăng ký:</strong> <a href="%s" style="color:#059669;text-decoration:none">%s</a>
                            </p>
                            <p style="margin:0;font-size:15px;color:#065f46">
                                <strong>Hỗ trợ:</strong> <a href="mailto:%s" style="color:#059669;text-decoration:none">%s</a> | %s
                            </p>
                        </div>
                    </div>
                    
                    <!-- Footer -->
                    <div style="padding:20px;background:#f9fafb;color:#6b7280;font-size:14px;text-align:center;border-top:1px solid #e5e7eb">
                        <p style="margin:0;font-weight:600;color:#111827">Trân trọng,</p>
                        <p style="margin:5px 0 0;color:#6b7280">Hệ thống quản lý đồ án tốt nghiệp</p>
                        <p style="margin:10px 0 0;font-size:12px;color:#9ca3af">Phenikaa University</p>
                    </div>
                </div>
            </body>
            </html>
            """,
                studentName,
                request.getPeriodName(),
                startDateStr,
                endDateStr,
                status,
                studentEmail,
                systemUrl,
                systemUrl,
                supportEmail,
                supportEmail,
                supportPhone
        );
    }

    // Helper class for email results
    private static class EmailResult {
        private final String email;
        private final boolean success;
        private final String errorMessage;

        public EmailResult(String email, boolean success, String errorMessage) {
            this.email = email;
            this.success = success;
            this.errorMessage = errorMessage;
        }

        public String getEmail() {
            return email;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }

    @PreDestroy
    public void cleanup() {
        log.info("Shutting down email executor service...");
        emailExecutor.shutdown();
        try {
            if (!emailExecutor.awaitTermination(30, TimeUnit.SECONDS)) {
                emailExecutor.shutdownNow();
                if (!emailExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
                    log.warn("Email executor did not terminate gracefully");
                }
            }
        } catch (InterruptedException e) {
            emailExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        log.info("Email executor service shutdown completed");
    }
}
