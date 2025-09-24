package com.phenikaa.thesisservice.scheduler;

import com.phenikaa.thesisservice.service.interfaces.RegistrationPeriodService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PeriodAutoCloseScheduler {

    private final RegistrationPeriodService registrationPeriodService;

    /**
     * Chạy mỗi 5 phút để kiểm tra và tự động đóng các period đã hết hạn
     */
    @Scheduled(fixedRate = 300000)
    public void autoCloseExpiredPeriods() {
        try {
            log.info("=== BẮT ĐẦU SCHEDULED TASK: TỰ ĐỘNG KẾT THÚC PERIOD ===");
            registrationPeriodService.autoCloseExpiredPeriods();
            log.info("=== HOÀN THÀNH SCHEDULED TASK: TỰ ĐỘNG KẾT THÚC PERIOD ===");
        } catch (Exception e) {
            log.error("Lỗi khi tự động kết thúc period: ", e);
        }
    }

    /**
     * Chạy mỗi giờ để kiểm tra chi tiết hơn
     */
    @Scheduled(cron = "0 0 * * * *")
    public void hourlyPeriodCheck() {
        try {
            log.info("=== BẮT ĐẦU HOURLY CHECK: KIỂM TRA PERIOD ===");
            registrationPeriodService.autoCloseExpiredPeriods();
            log.info("=== HOÀN THÀNH HOURLY CHECK: KIỂM TRA PERIOD ===");
        } catch (Exception e) {
            log.error("Lỗi khi kiểm tra period hàng giờ: ", e);
        }
    }
}
