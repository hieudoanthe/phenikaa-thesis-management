package com.phenikaa.userservice.dto.request;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserFilterRequest {
    
    // Thông tin cơ bản
    private String username;
    private String fullName;
    private Integer status;
    
    // Vai trò
    private List<String> roleNames; // STUDENT, ADMIN, TEACHER
    
    // Thời gian
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdFrom;
    
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdTo;
    
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lastLoginFrom;
    
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lastLoginTo;
    
    // Phân trang
    private Integer page = 0;
    private Integer size = 10;
    
    // Sắp xếp
    private String sortBy = "userId";
    private String sortDirection = "ASC"; // ASC hoặc DESC
    
    // Tìm kiếm theo pattern
    private String searchPattern; // Tìm kiếm trong username và fullName
}
