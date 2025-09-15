package com.phenikaa.userservice.controller;

import com.phenikaa.dto.response.GetUserResponse;
import com.phenikaa.userservice.service.interfaces.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/internal/users")
@RequiredArgsConstructor
@Slf4j
public class InternalStatisticsController {
    
    private final UserService userService;
    
    @GetMapping("/get-all-users")
    public List<GetUserResponse> getAllUsers() {
        log.info("Getting all users for statistics");
        return userService.getAllUsers();
    }
    
    @GetMapping("/get-users-by-role")
    public List<GetUserResponse> getUsersByRole(@RequestParam String role) {
        log.info("Getting users by role: {}", role);
        return userService.getUsersByRole(role);
    }
    
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
    
    @GetMapping("/get-user-count-by-status")
    public Long getUserCountByStatus(@RequestParam Integer status) {
        log.info("Getting user count by status: {}", status);
        return userService.getUserCountByStatus(status);
    }
    
    @GetMapping("/get-active-users-today")
    public Long getActiveUsersToday() {
        log.info("Getting active users today");
        return userService.getActiveUsersToday();
    }
}
