package com.phenikaa.thesisservice.service.implement;

import com.phenikaa.thesisservice.entity.RegistrationPeriod;
import com.phenikaa.thesisservice.repository.RegistrationPeriodRepository;
import com.phenikaa.thesisservice.service.interfaces.RegistrationPeriodService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class RegistrationPeriodServiceImpl implements RegistrationPeriodService {

    private final RegistrationPeriodRepository registrationPeriodRepository;

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
        
        RegistrationPeriod activePeriod = registrationPeriodRepository.findActivePeriod(now)
                .orElse(null);
        System.out.println("Kết quả tìm đợt đăng ký hiện tại: " + activePeriod); // Debug log
        
        if (activePeriod != null) {
            System.out.println("Tìm thấy đợt đăng ký: " + activePeriod.getPeriodId() + " - " + activePeriod.getPeriodName() + " - Status: " + activePeriod.getStatus()); // Debug log
        } else {
            System.out.println("KHÔNG tìm thấy đợt đăng ký nào!"); // Debug log
        }
        
        return activePeriod;
    }

    @Override
    public void startPeriod(Integer periodId) {
        RegistrationPeriod period = getPeriodById(periodId);
        if (period.canStart()) {
            period.setStatus(RegistrationPeriod.PeriodStatus.ACTIVE);
            registrationPeriodRepository.save(period);
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
}
