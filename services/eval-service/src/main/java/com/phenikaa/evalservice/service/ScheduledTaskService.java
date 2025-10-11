package com.phenikaa.evalservice.service;

import com.phenikaa.evalservice.entity.DefenseSession;
import com.phenikaa.evalservice.repository.DefenseSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduledTaskService {

    private final DefenseSessionRepository defenseSessionRepository;

    /**
     * Tự động cập nhật trạng thái defense session dựa trên ngày và thời gian
     * Chạy mỗi giờ một lần
     */
    @Scheduled(cron = "0 0 * * * *") // Chạy vào đầu mỗi giờ (00:00, 01:00, 02:00, ...)
    @Transactional
    public void updateSessionStatuses() {
        log.info("Bắt đầu cập nhật trạng thái defense sessions tự động");
        
        LocalDate today = LocalDate.now();
        LocalTime currentTime = LocalTime.now();
        LocalDateTime now = LocalDateTime.now();
        
        // Lấy tất cả sessions có trạng thái PLANNING hoặc SCHEDULED
        List<DefenseSession> sessionsToUpdate = defenseSessionRepository
                .findByStatusIn(Set.of(DefenseSession.SessionStatus.PLANNING, DefenseSession.SessionStatus.SCHEDULED));
        
        int updatedCount = 0;
        
        for (DefenseSession session : sessionsToUpdate) {
            DefenseSession.SessionStatus newStatus = determineNewStatus(session, today, currentTime);
            
            if (newStatus != null && newStatus != session.getStatus()) {
                session.setStatus(newStatus);
                defenseSessionRepository.save(session);
                updatedCount++;
                
                log.info("Đã cập nhật trạng thái session ID {} từ {} thành {}", 
                        session.getSessionId(), session.getStatus(), newStatus);
            }
        }
        
        log.info("Hoàn thành cập nhật trạng thái. Đã cập nhật {} sessions", updatedCount);
    }

    /**
     * Xác định trạng thái mới dựa trên ngày và thời gian hiện tại
     */
    private DefenseSession.SessionStatus determineNewStatus(DefenseSession session, 
                                                           LocalDate today, 
                                                           LocalTime currentTime) {
        
        LocalDate defenseDate = session.getDefenseDate();
        LocalDateTime startTime = session.getStartTime();
        LocalDateTime endTime = session.getEndTime();
        
        if (defenseDate == null || startTime == null || endTime == null) {
            return null; // Không thể xác định trạng thái nếu thiếu thông tin
        }
        
        LocalDate startDate = startTime.toLocalDate();
        LocalTime startTimeOnly = startTime.toLocalTime();
        LocalTime endTimeOnly = endTime.toLocalTime();
        
        // Tính số ngày trước khi diễn ra
        long daysUntilDefense = java.time.temporal.ChronoUnit.DAYS.between(today, startDate);
        
        // Nếu đã qua ngày kết thúc (sau 7PM)
        if (today.isAfter(startDate) || 
            (today.equals(startDate) && currentTime.isAfter(LocalTime.of(19, 0)))) {
            return DefenseSession.SessionStatus.COMPLETED;
        }
        
        // Nếu đang trong ngày diễn ra và trong khoảng thời gian bảo vệ
        if (today.equals(startDate) && 
            currentTime.isAfter(startTimeOnly) && 
            currentTime.isBefore(endTimeOnly)) {
            return DefenseSession.SessionStatus.IN_PROGRESS;
        }
        
        // Nếu cách 5 ngày trước khi diễn ra
        if (daysUntilDefense <= 5 && daysUntilDefense >= 0) {
            return DefenseSession.SessionStatus.SCHEDULED;
        }
        
        // Nếu còn hơn 5 ngày thì giữ nguyên PLANNING
        return null; // Không thay đổi trạng thái
    }

    /**
     * Method để test logic chuyển trạng thái (có thể gọi thủ công)
     */
    public void updateSessionStatusesManually() {
        log.info("Chạy cập nhật trạng thái defense sessions thủ công");
        updateSessionStatuses();
    }
}
