package com.phenikaa.thesisservice.service.implement;

import com.phenikaa.thesisservice.entity.RegistrationPeriod;
import com.phenikaa.thesisservice.repository.RegistrationPeriodRepository;
import com.phenikaa.thesisservice.service.interfaces.RegistrationPeriodService;
import com.phenikaa.thesisservice.client.UserServiceClient;
import com.phenikaa.thesisservice.client.NotificationServiceClient;
import com.phenikaa.thesisservice.dto.response.StudentsByPeriodResponse;
import java.util.Map;
import com.phenikaa.thesisservice.dto.request.NotificationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class RegistrationPeriodServiceImpl implements RegistrationPeriodService {

    private final RegistrationPeriodRepository registrationPeriodRepository;
    private final UserServiceClient userServiceClient;
    private final NotificationServiceClient notificationServiceClient;
    @Override
    public RegistrationPeriod createPeriod(RegistrationPeriod period) {
        // Đảm bảo luôn tạo mới - xóa periodId nếu có
        period.setPeriodId(null);
        period.setCreatedAt(LocalDateTime.now());
        period.setUpdatedAt(LocalDateTime.now());
        
        // Set status mặc định nếu chưa có
        if (period.getStatus() == null) {
            period.setStatus(RegistrationPeriod.PeriodStatus.UPCOMING);
        }
        
        System.out.println("Đang tạo đợt đăng ký mới: " + period); // Debug log
        RegistrationPeriod savedPeriod = registrationPeriodRepository.save(period);
        System.out.println("Đã tạo đợt đăng ký thành công với ID: " + savedPeriod.getPeriodId()); // Debug log

        // Kiểm tra nếu ngày bắt đầu là ngày hiện tại thì tự động bắt đầu (chỉ khi đã có sinh viên)
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDate = savedPeriod.getStartDate();
        
        // So sánh ngày (bỏ qua giờ)
        LocalDateTime todayStart = now.toLocalDate().atStartOfDay();
        LocalDateTime periodStart = startDate.toLocalDate().atStartOfDay();
        
        if (periodStart.equals(todayStart) || periodStart.isBefore(todayStart)) {
            log.info("Đợt đăng ký {} có ngày bắt đầu là hôm nay hoặc đã qua, kiểm tra sinh viên...", savedPeriod.getPeriodName());
            try {
                // Kiểm tra xem có sinh viên nào trong đợt đăng ký không
                StudentsByPeriodResponse response = userServiceClient.getStudentsByPeriod(savedPeriod.getPeriodId());
                if (response.isSuccess() && response.getData() != null && !response.getData().isEmpty()) {
                    log.info("Đợt đăng ký {} có {} sinh viên, tự động bắt đầu...", savedPeriod.getPeriodName(), response.getData().size());
                    startPeriod(savedPeriod.getPeriodId());
                    log.info("Đã tự động bắt đầu đợt đăng ký: {}", savedPeriod.getPeriodName());
                } else {
                    log.info("Đợt đăng ký {} chưa có sinh viên, chờ import sinh viên trước khi bắt đầu", savedPeriod.getPeriodName());
                }
            } catch (Exception e) {
                log.error("Lỗi khi kiểm tra sinh viên cho đợt đăng ký {}: {}", savedPeriod.getPeriodName(), e.getMessage(), e);
            }
        }

        return savedPeriod;
    }

    @Override
    public RegistrationPeriod updatePeriod(RegistrationPeriod period) {
        if (!registrationPeriodRepository.existsById(period.getPeriodId())) {
            throw new RuntimeException("Registration period not found with id: " + period.getPeriodId());
        }
        return registrationPeriodRepository.save(period);
    }

    @Override
    public void deletePeriod(Integer periodId) {
        if (!registrationPeriodRepository.existsById(periodId)) {
            throw new RuntimeException("Registration period not found with id: " + periodId);
        }
        registrationPeriodRepository.deleteById(periodId);
    }

    @Override
    public RegistrationPeriod getPeriodById(Integer periodId) {
        return registrationPeriodRepository.findById(periodId)
                .orElseThrow(() -> new RuntimeException("Registration period not found with id: " + periodId));
    }

    @Override
    public List<RegistrationPeriod> getAllPeriods() {
        return registrationPeriodRepository.findAll();
    }

    @Override
    public List<RegistrationPeriod> getPeriodsByAcademicYear(Integer academicYearId) {
        if (academicYearId != null) {
            return registrationPeriodRepository.findByAcademicYearId(academicYearId);
        } else {
            return registrationPeriodRepository.findAll();
        }
    }

    @Override
    public RegistrationPeriod getCurrentActivePeriod() {
        System.out.println("=== Đang tìm đợt đăng ký hiện tại ==="); // Debug log
        LocalDateTime now = LocalDateTime.now();
        System.out.println("Thời gian hiện tại: " + now); // Debug log
        
        // Lấy tất cả đợt đăng ký để debug
        List<RegistrationPeriod> allPeriods = registrationPeriodRepository.findAll();
        System.out.println("Tất cả đợt đăng ký trong DB: " + allPeriods.size()); // Debug log
        for (RegistrationPeriod p : allPeriods) {
            System.out.println("Period: " + p.getPeriodId() + " - " + p.getPeriodName() + " - Status: " + p.getStatus() + " - Start: " + p.getStartDate() + " - End: " + p.getEndDate()); // Debug log
        }
        
        // Chỉ tìm các đợt có status = 'ACTIVE' và đang trong khoảng thời gian hiện tại
        List<RegistrationPeriod> activePeriods = registrationPeriodRepository.findAllActivePeriods(now);
        System.out.println("Số đợt ACTIVE tìm thấy: " + activePeriods.size()); // Debug log
        
        RegistrationPeriod activePeriod = null;
        if (!activePeriods.isEmpty()) {
            // Nếu có nhiều đợt ACTIVE, lấy đợt có startDate gần nhất (mới nhất)
            activePeriod = activePeriods.stream()
                .max((p1, p2) -> p1.getStartDate().compareTo(p2.getStartDate()))
                .orElse(null);
        }
        
        System.out.println("Kết quả tìm đợt đăng ký hiện tại: " + activePeriod); // Debug log
        
        if (activePeriod != null) {
            System.out.println("Tìm thấy đợt đăng ký: " + activePeriod.getPeriodId() + " - " + activePeriod.getPeriodName() + " - Status: " + activePeriod.getStatus()); // Debug log
        } else {
            System.out.println("KHÔNG tìm thấy đợt đăng ký ACTIVE nào!"); // Debug log
        }
        
        return activePeriod;
    }

    @Override
    public List<RegistrationPeriod> getAllActivePeriods() {
        return registrationPeriodRepository.findAllActivePeriods(LocalDateTime.now());
    }

    @Override
    public void startPeriod(Integer periodId) {
        RegistrationPeriod period = getPeriodById(periodId);
        if (period.canStart()) {
            period.setStatus(RegistrationPeriod.PeriodStatus.ACTIVE);
            registrationPeriodRepository.save(period);
            
            // Gửi email thông báo khi admin bắt đầu thủ công
            try {
                StudentsByPeriodResponse response = userServiceClient.getStudentsByPeriod(periodId);
                if (response.isSuccess() && response.getData() != null) {
                    for (Map<String, Object> student : response.getData()) {
                        String emailContent = buildStartEmailContent(period);
                        NotificationRequest notificationRequest = new NotificationRequest();
                        notificationRequest.setSenderId(1); // System sender
                        notificationRequest.setReceiverId((Integer) student.get("userId")); // Sử dụng userId thay vì studentId
                        notificationRequest.setType("REGISTRATION_PERIOD");
                        notificationRequest.setMessage(emailContent);
                        
                        notificationServiceClient.sendNotification(notificationRequest);
                    }
                } else {
                    log.warn("No students found for period {} or API returned error: {}", periodId, response.getMessage());
                }
            } catch (Exception e) {
                log.error("Error sending email notifications for period {}: {}", periodId, e.getMessage(), e);
            }
        } else {
            throw new RuntimeException("Cannot start period: " + period.getPeriodName());
        }
    }

    @Override
    public void closePeriod(Integer periodId) {
        RegistrationPeriod period = getPeriodById(periodId);
        if (period.canClose()) {
            period.setStatus(RegistrationPeriod.PeriodStatus.CLOSED);
            registrationPeriodRepository.save(period);
        } else {
            throw new RuntimeException("Cannot close period: " + period.getPeriodName());
        }
    }

    @Override
    public List<RegistrationPeriod> getUpcomingPeriods() {
        return registrationPeriodRepository.findUpcomingPeriods(LocalDateTime.now());
    }

    @Override
    public void autoCloseExpiredPeriods() {
        System.out.println("=== TỰ ĐỘNG KẾT THÚC CÁC PERIOD ĐÃ HẾT HẠN ===");
        LocalDateTime now = LocalDateTime.now();
        
        // Lấy tất cả period đang ACTIVE
        List<RegistrationPeriod> activePeriods = registrationPeriodRepository.findByStatus(RegistrationPeriod.PeriodStatus.ACTIVE);
        
        for (RegistrationPeriod period : activePeriods) {
            if (period.shouldAutoClose()) {
                System.out.println("Tự động kết thúc period: " + period.getPeriodName() + " (ID: " + period.getPeriodId() + ")");
                period.setStatus(RegistrationPeriod.PeriodStatus.CLOSED);
                registrationPeriodRepository.save(period);
                System.out.println("Đã tự động kết thúc period: " + period.getPeriodName());
            }
        }
    }

    @Override
    public void checkAndAutoClosePeriod(Integer periodId) {
        System.out.println("=== KIỂM TRA VÀ TỰ ĐỘNG KẾT THÚC PERIOD ===");
        System.out.println("Period ID: " + periodId);
        
        RegistrationPeriod period = getPeriodById(periodId);
        System.out.println("Period: " + period.getPeriodName());
        System.out.println("Status: " + period.getStatus());
        System.out.println("End Date: " + period.getEndDate());
        
        LocalDateTime now = LocalDateTime.now();
        System.out.println("Thời gian hiện tại: " + now);
        
        if (period.shouldAutoClose()) {
            System.out.println("Period đã hết hạn - tự động kết thúc...");
            period.setStatus(RegistrationPeriod.PeriodStatus.CLOSED);
            registrationPeriodRepository.save(period);
            System.out.println("Đã tự động kết thúc period: " + period.getPeriodName());
        } else {
            System.out.println("Period chưa hết hạn hoặc không thể tự động kết thúc");
        }
    }

    private String buildStartEmailContent(RegistrationPeriod period) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String startDate = period.getStartDate().format(dateFormatter);
        String endDate = period.getEndDate().format(dateFormatter);
        String startTime = period.getStartDate().format(DateTimeFormatter.ofPattern("HH:mm"));
        String endTime = period.getEndDate().format(DateTimeFormatter.ofPattern("HH:mm"));
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
            period.getPeriodName(), startDate, startTime, endDate, endTime, currentDateTime);
    }
}
