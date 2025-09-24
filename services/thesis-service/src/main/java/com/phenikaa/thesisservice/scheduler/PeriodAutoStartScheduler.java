package com.phenikaa.thesisservice.scheduler;

import com.phenikaa.thesisservice.entity.RegistrationPeriod;
import com.phenikaa.thesisservice.repository.RegistrationPeriodRepository;
import com.phenikaa.thesisservice.client.NotificationServiceClient;
import com.phenikaa.thesisservice.client.UserServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class PeriodAutoStartScheduler {

    private final RegistrationPeriodRepository registrationPeriodRepository;
    private final NotificationServiceClient notificationServiceClient;
    private final UserServiceClient userServiceClient;

    // Chạy mỗi phút
    @Scheduled(cron = "0 * * * * *")
    public void autoStartAndNotify() {
        LocalDateTime now = LocalDateTime.now();
        List<RegistrationPeriod> upcoming = registrationPeriodRepository.findUpcomingPeriods(now);
        for (RegistrationPeriod p : upcoming) {
            if (p.getStartDate() != null && !p.getStartDate().isAfter(now)) {
                try {
                    // Activate
                    p.setStatus(RegistrationPeriod.PeriodStatus.ACTIVE);
                    registrationPeriodRepository.save(p);

                    // Fetch students of this period
                    List<Map<String, Object>> students = userServiceClient.getStudentsByPeriod(p.getPeriodId());

                    // Compose and send message per student via communication-log-service
                    for (Map<String, Object> s : students) {
                        try {
                            Integer userId = (Integer) s.get("userId");
                            com.phenikaa.thesisservice.dto.request.NotificationRequest req =
                                    new com.phenikaa.thesisservice.dto.request.NotificationRequest(
                                            0, userId, buildStartEmailContent(p), "REGISTRATION_PERIOD");
                            notificationServiceClient.sendNotification(req);
                        } catch (Exception ex) {
                            log.warn("Không thể gửi thông báo cho user trong period {}: {}", p.getPeriodId(), ex.getMessage());
                        }
                    }
                    log.info("Đã tự động kích hoạt period {} và gửi thông báo cho {} sinh viên", p.getPeriodId(), students.size());
                } catch (Exception ex) {
                    log.error("Lỗi auto-start period {}: {}", p.getPeriodId(), ex.getMessage(), ex);
                }
            }
        }
    }

    private String buildStartEmailContent(RegistrationPeriod p) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String startDate = p.getStartDate().format(dateFormatter);
        String endDate = p.getEndDate().format(dateFormatter);
        String startTime = p.getStartDate().format(DateTimeFormatter.ofPattern("HH:mm"));
        String endTime = p.getEndDate().format(DateTimeFormatter.ofPattern("HH:mm"));
        String currentDateTime = LocalDateTime.now().format(dateTimeFormatter);

        return String.format("""
            <html>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                <div style="max-width: 600px; margin: 20px auto; padding: 20px; border: 1px solid #ddd; border-radius: 8px; background-color: #f9f9f9;">
                    <h2 style="color: #0056b3; text-align: center;">THÔNG BÁO MỞ ĐỢT ĐĂNG KÝ ĐỀ TÀI KHÓA LUẬN</h2>
                    <p>Kính gửi Quý Sinh viên,</p>
                    <p>Nhà trường xin trân trọng thông báo về việc mở đợt đăng ký đề tài khóa luận mới:</p>
                    
                    <div style="background-color: #e9f7ff; border-left: 5px solid #007bff; padding: 15px; margin: 20px 0; border-radius: 4px;">
                        <p style="margin: 0;"><strong>Tên đợt đăng ký:</strong> %s</p>
                        <p style="margin: 0;"><strong>Thời gian bắt đầu:</strong> %s vào lúc %s</p>
                        <p style="margin: 0;"><strong>Thời gian kết thúc:</strong> %s vào lúc %s</p>
                    </div>
                    
                    <p>Đề nghị Quý Sinh viên kiểm tra kỹ thông tin chi tiết về các đề tài, điều kiện đăng ký, và hướng dẫn nộp hồ sơ trên hệ thống quản lý luận văn của Trường.</p>
                    <p>Vui lòng lưu ý các mốc thời gian quan trọng để đảm bảo việc đăng ký diễn ra thuận lợi và đúng hạn.</p>
                    
                    <p>Mọi thắc mắc xin vui lòng liên hệ Phòng Công nghệ Thông tin để được hỗ trợ.</p>
                    
                    <p>Trân trọng,</p>
                    <p><strong>Trường Đại học Phenikaa</strong></p>
                    <hr style="border-top: 1px solid #eee; margin: 20px 0;">
                    <p style="font-size: 0.8em; color: #777; text-align: center;">Email được gửi tự động từ Hệ thống quản lý luận văn Phenikaa University vào lúc %s.</p>
                </div>
            </body>
            </html>
            """,
            p.getPeriodName(), startDate, startTime, endDate, endTime, currentDateTime);
    }
}


