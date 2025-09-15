package com.phenikaa.assignservice.service;

import com.phenikaa.assignservice.dto.request.CreateAssignmentRequest;
import com.phenikaa.assignservice.dto.request.UpdateAssignmentRequest;
import com.phenikaa.assignservice.dto.request.CreateTaskRequest;
import com.phenikaa.assignservice.dto.request.UpdateTaskRequest;
import com.phenikaa.assignservice.dto.response.TaskResponse;
import com.phenikaa.assignservice.dto.response.AssignmentResponse;

import java.util.List;
import java.util.Map;

public interface AssignmentService {
    
    /**
     * Tạo assignment mới
     */
    AssignmentResponse createAssignment(CreateAssignmentRequest request, Integer assignedBy);
    
    /**
     * Cập nhật assignment
     */
    AssignmentResponse updateAssignment(Integer assignmentId, UpdateAssignmentRequest request);
    
    /**
     * Lấy assignment theo ID
     */
    AssignmentResponse getAssignmentById(Integer assignmentId);
    
    /**
     * Lấy tất cả assignments theo topicId
     */
    List<AssignmentResponse> getAssignmentsByTopicId(Integer topicId);
    
    /**
     * Lấy assignments theo người được phân công
     */
    List<AssignmentResponse> getAssignmentsByAssignedTo(Integer assignedTo);
    
    /**
     * Lấy assignments theo người phân công
     */
    List<AssignmentResponse> getAssignmentsByAssignedBy(Integer assignedBy);
    
    /**
     * Tạo task thuộc một assignment
     */
    TaskResponse createTask(Integer assignmentId, CreateTaskRequest request);

    TaskResponse updateTask(Integer taskId, UpdateTaskRequest request);

    boolean deleteTask(Integer taskId);
    
    /**
     * Xóa assignment
     */
    boolean deleteAssignment(Integer assignmentId);
    
    /**
     * Cập nhật trạng thái assignment
     */
    AssignmentResponse updateAssignmentStatus(Integer assignmentId, Integer status);
    
    // Statistics methods
    Long getAssignmentCount();
    Long getAssignmentCountByStatus(String status);
    Long getAssignmentCountByUser(Integer userId);
    Long getAssignmentCountByTopic(Integer topicId);
    List<Map<String, Object>> getAssignmentsByUser(Integer userId);
    List<Map<String, Object>> getAssignmentsByTopic(Integer topicId);
    Long getTaskCount();
    Long getTaskCountByStatus(String status);
    Long getTaskCountByAssignment(Integer assignmentId);
    List<Map<String, Object>> getTasksByAssignment(Integer assignmentId);
}
