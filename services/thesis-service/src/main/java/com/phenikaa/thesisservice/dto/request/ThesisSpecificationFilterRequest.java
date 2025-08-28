package com.phenikaa.thesisservice.dto.request;

import com.phenikaa.thesisservice.entity.ProjectTopic;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.Instant;

@Data
public class ThesisSpecificationFilterRequest {
    
    // Thông tin cơ bản
    private String topicCode;
    private String title;
    private String description;
    private String objectives;
    private String methodology;
    
    // Thông tin giảng viên và năm học
    private Integer supervisorId;
    private Integer academicYearId;
    
    // Số lượng sinh viên
    private Integer minStudents;
    private Integer maxStudents;
    
    // Độ khó và trạng thái
    private ProjectTopic.DifficultyLevel difficultyLevel;
    private ProjectTopic.TopicStatus topicStatus;
    private ProjectTopic.ApprovalStatus approvalStatus;
    
    // Thời gian tạo và cập nhật
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private Instant createdFrom;
    
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private Instant createdTo;
    
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private Instant updatedFrom;
    
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private Instant updatedTo;
    
    // Người tạo và cập nhật
    private Integer createdBy;
    private Integer updatedBy;
    
    // Tìm kiếm theo pattern
    private String searchPattern;
    
    // Phân trang và sắp xếp
    private Integer page = 0;
    private Integer size = 10;
    private String sortBy = "topicId";
    private String sortDirection = "ASC";
    
    // Lọc theo vai trò người dùng
    private String userRole;
    private Integer userId;
}
