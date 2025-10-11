package com.phenikaa.userservice.controller;

import com.phenikaa.userservice.service.interfaces.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/users")
@RequiredArgsConstructor
@Slf4j
public class InternalStatisticsController {
    
    private final UserService userService;

    @GetMapping("/get-user-count")
    public Long getUserCount() {
        log.info("Đang lấy tổng số người dùng");
        return userService.getUserCount();
    }
    
    @GetMapping("/get-user-count-by-role")
    public Long getUserCountByRole(@RequestParam String role) {
        log.info("Đang lấy số lượng người dùng theo vai trò: {}", role);
        return userService.getUserCountByRole(role);
    }
    
    @GetMapping("/get-student-count-by-period")
    public Long getStudentCountByPeriod(@RequestParam Integer periodId) {
        log.info("Đang lấy số lượng sinh viên theo đợt: {}", periodId);
        return userService.getStudentCountByPeriod(periodId);
    }
}
