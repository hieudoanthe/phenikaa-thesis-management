package com.phenikaa.thesisservice.service;

import java.util.Map;

public interface PeriodStatisticsService {
    
    /**
     * Lấy thống kê tổng quan của một đợt đăng ký
     */
    Map<String, Object> getPeriodOverview(Integer periodId);
    
    /**
     * Lấy tổng số sinh viên của một đợt (đã đăng ký + chưa đăng ký)
     */
    Integer getTotalStudents(Integer periodId);
    
    /**
     * Lấy số sinh viên đã đăng ký/đề xuất của một đợt
     */
    Integer getRegisteredStudents(Integer periodId);
    
    /**
     * Lấy số sinh viên chưa đăng ký/đề xuất của một đợt
     */
    Integer getUnregisteredStudents(Integer periodId);
}
