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
    
    @GetMapping("/get-teacher-profile-count")
    public Long getLecturerProfileCount() {
        log.info("Getting lecturer profile count");
        return profileService.getLecturerProfileCount();
    }
}
