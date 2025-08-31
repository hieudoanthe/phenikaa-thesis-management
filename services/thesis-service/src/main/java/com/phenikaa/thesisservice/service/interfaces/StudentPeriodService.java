package com.phenikaa.thesisservice.service.interfaces;

import com.phenikaa.thesisservice.dto.response.GetStudentPeriodResponse;
import java.util.List;

public interface StudentPeriodService {
    
    /**
     * Lấy danh sách sinh viên đã đăng ký đề tài theo đợt đăng ký
     * @param periodId ID của đợt đăng ký
     * @return Danh sách sinh viên đã đăng ký
     */
    List<GetStudentPeriodResponse> getStudentsByPeriod(Integer periodId);
    
    /**
     * Lấy danh sách sinh viên đã đề xuất đề tài theo đợt đăng ký
     * @param periodId ID của đợt đăng ký
     * @return Danh sách sinh viên đã đề xuất
     */
    List<GetStudentPeriodResponse> getSuggestedStudentsByPeriod(Integer periodId);
    
    /**
     * Lấy danh sách tất cả sinh viên (đăng ký + đề xuất) theo đợt đăng ký
     * @param periodId ID của đợt đăng ký
     * @return Danh sách tất cả sinh viên
     */
    List<GetStudentPeriodResponse> getAllStudentsByPeriod(Integer periodId);
}
