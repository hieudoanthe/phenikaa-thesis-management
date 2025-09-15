package com.phenikaa.thesisservice.controller;

import com.phenikaa.thesisservice.service.interfaces.ThesisService;
import com.phenikaa.thesisservice.service.interfaces.RegisterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/internal/thesis")
@RequiredArgsConstructor
@Slf4j
public class InternalStatisticsController {
    
    private final ThesisService thesisService;
    private final RegisterService registerService;
    
    @GetMapping("/get-topic-count")
    public Long getTopicCount() {
        log.info("Getting total topic count");
        return thesisService.getTopicCount();
    }
    
    @GetMapping("/get-topic-count-by-status")
    public Long getTopicCountByStatus(@RequestParam String status) {
        log.info("Getting topic count by status: {}", status);
        return thesisService.getTopicCountByStatus(status);
    }
    
    @GetMapping("/get-topic-count-by-difficulty")
    public Long getTopicCountByDifficulty(@RequestParam String difficulty) {
        log.info("Getting topic count by difficulty: {}", difficulty);
        return thesisService.getTopicCountByDifficulty(difficulty);
    }
    
    @GetMapping("/get-topic-count-by-academic-year")
    public Long getTopicCountByAcademicYear(@RequestParam Integer academicYearId) {
        log.info("Getting topic count by academic year: {}", academicYearId);
        return thesisService.getTopicCountByAcademicYear(academicYearId);
    }
    
    @GetMapping("/get-topic-count-by-supervisor")
    public Long getTopicCountBySupervisor(@RequestParam Integer supervisorId) {
        log.info("Getting topic count by supervisor: {}", supervisorId);
        return thesisService.getTopicCountBySupervisor(supervisorId);
    }
    
    @GetMapping("/get-registration-count")
    public Long getRegistrationCount() {
        log.info("Getting total registration count");
        return registerService.getRegistrationCount();
    }
    
    @GetMapping("/get-registration-count-by-status")
    public Long getRegistrationCountByStatus(@RequestParam String status) {
        log.info("Getting registration count by status: {}", status);
        return registerService.getRegistrationCountByStatus(status);
    }
    
    @GetMapping("/get-registration-count-by-academic-year")
    public Long getRegistrationCountByAcademicYear(@RequestParam Integer academicYearId) {
        log.info("Getting registration count by academic year: {}", academicYearId);
        return registerService.getRegistrationCountByAcademicYear(academicYearId);
    }
    
    @GetMapping("/get-topics-by-supervisor")
    public List<Map<String, Object>> getTopicsBySupervisor(@RequestParam Integer supervisorId) {
        log.info("Getting topics by supervisor: {}", supervisorId);
        return thesisService.getTopicsBySupervisor(supervisorId);
    }
    
    @GetMapping("/get-topics-stats-by-supervisor")
    public Map<String, Object> getTopicsStatsBySupervisor(@RequestParam Integer supervisorId) {
        log.info("Getting topics stats by supervisor: {}", supervisorId);
        return thesisService.getTopicsStatsBySupervisor(supervisorId);
    }
    
    @GetMapping("/get-registrations-by-topic")
    public List<Map<String, Object>> getRegistrationsByTopic(@RequestParam Integer topicId) {
        log.info("Getting registrations by topic: {}", topicId);
        return registerService.getRegistrationsByTopic(topicId);
    }
    
    @GetMapping("/get-topics-over-time")
    public List<Map<String, Object>> getTopicsOverTime(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        log.info("Getting topics over time from {} to {}", startDate, endDate);
        return thesisService.getTopicsOverTime(startDate, endDate);
    }
    
    @GetMapping("/get-registrations-over-time")
    public List<Map<String, Object>> getRegistrationsOverTime(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        log.info("Getting registrations over time from {} to {}", startDate, endDate);
        return registerService.getRegistrationsOverTime(startDate, endDate);
    }
    
    @GetMapping("/get-registrations-today")
    public Long getRegistrationsToday() {
        log.info("Getting registrations count for today");
        return registerService.getRegistrationsToday();
    }
    
    @GetMapping("/get-today-registrations")
    public List<Map<String, Object>> getTodayRegistrations() {
        log.info("Getting today's registrations");
        return registerService.getTodayRegistrations();
    }
}
