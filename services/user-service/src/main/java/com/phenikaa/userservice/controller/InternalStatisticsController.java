package com.phenikaa.userservice.controller;

import com.phenikaa.dto.response.GetUserResponse;
import com.phenikaa.userservice.service.interfaces.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/internal/users")
@RequiredArgsConstructor
@Slf4j
public class InternalStatisticsController {
    
    private final UserService userService;

    @GetMapping("/get-user-count")
    public Long getUserCount() {
        log.info("Getting total user count");
        return userService.getUserCount();
    }
    
    @GetMapping("/get-user-count-by-role")
    public Long getUserCountByRole(@RequestParam String role) {
        log.info("Getting user count by role: {}", role);
        return userService.getUserCountByRole(role);
    }
    
    @GetMapping("/get-student-count-by-period")
    public Long getStudentCountByPeriod(@RequestParam Integer periodId) {
        log.info("Getting student count by period: {}", periodId);
        return userService.getStudentCountByPeriod(periodId);
    }
}
