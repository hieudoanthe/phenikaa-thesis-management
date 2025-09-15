package com.phenikaa.thesisservice.service.interfaces;

import com.phenikaa.thesisservice.dto.request.RegisterTopicRequest;

import java.util.List;
import java.util.Map;

public interface RegisterService {
    void registerTopic(RegisterTopicRequest dto, Integer userId);
    
    // Statistics methods
    Long getRegistrationCount();
    Long getRegistrationCountByStatus(String status);
    Long getRegistrationCountByAcademicYear(Integer academicYearId);
    List<Map<String, Object>> getRegistrationsByTopic(Integer topicId);
    List<Map<String, Object>> getRegistrationsOverTime(String startDate, String endDate);
    Long getRegistrationsToday();
    List<Map<String, Object>> getTodayRegistrations();
}
