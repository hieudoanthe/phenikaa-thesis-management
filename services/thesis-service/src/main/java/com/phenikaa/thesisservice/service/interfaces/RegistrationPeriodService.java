package com.phenikaa.thesisservice.service.interfaces;

import com.phenikaa.thesisservice.entity.RegistrationPeriod;
import java.util.List;

public interface RegistrationPeriodService {
    
    RegistrationPeriod createPeriod(RegistrationPeriod period);
    
    RegistrationPeriod updatePeriod(RegistrationPeriod period);
    
    void deletePeriod(Integer periodId);
    
    RegistrationPeriod getPeriodById(Integer periodId);
    
    List<RegistrationPeriod> getAllPeriods();
    
    List<RegistrationPeriod> getPeriodsByAcademicYear(Integer academicYearId);
    
    RegistrationPeriod getCurrentActivePeriod();
    
    // Trả về tất cả đợt ACTIVE tại thời điểm hiện tại (cho phép song song)
    List<RegistrationPeriod> getAllActivePeriods();
    
    void startPeriod(Integer periodId);
    
    void closePeriod(Integer periodId);
    
    List<RegistrationPeriod> getUpcomingPeriods();
    
    // Tự động kết thúc các period đã hết hạn
    void autoCloseExpiredPeriods();
    
    // Kiểm tra và kết thúc period cụ thể nếu cần
    void checkAndAutoClosePeriod(Integer periodId);
}
