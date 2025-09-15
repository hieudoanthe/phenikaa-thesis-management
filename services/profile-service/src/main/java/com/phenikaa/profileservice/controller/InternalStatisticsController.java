package com.phenikaa.profileservice.controller;

import com.phenikaa.profileservice.service.interfaces.ProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/internal/profiles")
@RequiredArgsConstructor
@Slf4j
public class InternalStatisticsController {
    
    private final ProfileService profileService;
    
    @GetMapping("/get-profile-count")
    public Long getProfileCount() {
        log.info("Getting total profile count");
        return profileService.getProfileCount();
    }
    
    @GetMapping("/get-student-profile-count")
    public Long getStudentProfileCount() {
        log.info("Getting student profile count");
        return profileService.getStudentProfileCount();
    }
    
    @GetMapping("/get-lecturer-profile-count")
    public Long getLecturerProfileCount() {
        log.info("Getting lecturer profile count");
        return profileService.getLecturerProfileCount();
    }
    
    @GetMapping("/get-profiles-by-major")
    public List<Map<String, Object>> getProfilesByMajor(@RequestParam String major) {
        log.info("Getting profiles by major: {}", major);
        return profileService.getProfilesByMajor(major);
    }
    
    @GetMapping("/get-profiles-by-year")
    public List<Map<String, Object>> getProfilesByYear(@RequestParam Integer year) {
        log.info("Getting profiles by year: {}", year);
        return profileService.getProfilesByYear(year);
    }
    
    @GetMapping("/get-student-profiles-by-supervisor")
    public List<Map<String, Object>> getStudentProfilesBySupervisor(@RequestParam Integer supervisorId) {
        log.info("Getting student profiles by supervisor: {}", supervisorId);
        return profileService.getStudentProfilesBySupervisor(supervisorId);
    }
    
    @GetMapping("/get-profile-by-user-id")
    public Map<String, Object> getProfileByUserId(@RequestParam Integer userId) {
        log.info("Getting profile by user id: {}", userId);
        return profileService.getProfileByUserId(userId);
    }
}
