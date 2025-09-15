package com.phenikaa.assignservice.service.impl;

import com.phenikaa.assignservice.dto.request.CreateAssignmentRequest;
import com.phenikaa.assignservice.dto.request.UpdateAssignmentRequest;
import com.phenikaa.assignservice.dto.request.CreateTaskRequest;
import com.phenikaa.assignservice.dto.request.UpdateTaskRequest;
import com.phenikaa.assignservice.dto.response.AssignmentResponse;
import com.phenikaa.assignservice.dto.response.TaskResponse;
import com.phenikaa.assignservice.entity.Assignment;
import com.phenikaa.assignservice.entity.Task;
import com.phenikaa.assignservice.repository.AssignmentRepository;
import com.phenikaa.assignservice.repository.TaskRepository;
import com.phenikaa.assignservice.service.AssignmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AssignmentServiceImpl implements AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final TaskRepository taskRepository;

    @Override
    public AssignmentResponse createAssignment(CreateAssignmentRequest request, Integer assignedBy) {
        try {
            log.info("Tạo assignment mới cho topicId: {}, assignedTo: {}", request.getTopicId(), request.getAssignedTo());
            
            // Tạo assignment mới
            Assignment assignment = new Assignment();
            assignment.setTopicId(request.getTopicId());
            assignment.setAssignedTo(request.getAssignedTo());
            assignment.setAssignedBy(assignedBy);
            assignment.setTitle(request.getTitle());
            assignment.setDescription(request.getDescription());
            assignment.setDueDate(request.getDueDate());
            assignment.setPriority(request.getPriority());
            assignment.setStatus(1);
            assignment.setCreatedAt(Instant.now());
            assignment.setUpdatedAt(Instant.now());
            
            // Lưu assignment
            Assignment savedAssignment = assignmentRepository.save(assignment);
            log.info("Đã tạo assignment thành công với ID: {}", savedAssignment.getAssignmentId());
            
            return convertToResponse(savedAssignment);
        } catch (Exception e) {
            log.error("Lỗi khi tạo assignment: {}", e.getMessage(), e);
            throw new RuntimeException("Không thể tạo assignment: " + e.getMessage());
        }
    }

    @Override
    public AssignmentResponse updateAssignment(Integer assignmentId, UpdateAssignmentRequest request) {
        try {
            log.info("Cập nhật assignment với ID: {}", assignmentId);
            
            Assignment assignment = assignmentRepository.findById(assignmentId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy assignment với ID: " + assignmentId));
            
            // Cập nhật thông tin
            if (request.getTitle() != null) {
                assignment.setTitle(request.getTitle());
            }
            if (request.getDescription() != null) {
                assignment.setDescription(request.getDescription());
            }
            if (request.getDueDate() != null) {
                assignment.setDueDate(request.getDueDate());
            }
            if (request.getPriority() != null) {
                assignment.setPriority(request.getPriority());
            }
            if (request.getStatus() != null) {
                assignment.setStatus(request.getStatus());
            }
            
            assignment.setUpdatedAt(Instant.now());
            
            Assignment updatedAssignment = assignmentRepository.save(assignment);
            log.info("Đã cập nhật assignment thành công với ID: {}", assignmentId);
            
            return convertToResponse(updatedAssignment);
        } catch (Exception e) {
            log.error("Lỗi khi cập nhật assignment: {}", e.getMessage(), e);
            throw new RuntimeException("Không thể cập nhật assignment: " + e.getMessage());
        }
    }

    @Override
    public AssignmentResponse getAssignmentById(Integer assignmentId) {
        try {
            log.info("Lấy assignment theo ID: {}", assignmentId);
            
            Assignment assignment = assignmentRepository.findById(assignmentId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy assignment với ID: " + assignmentId));
            
            return convertToResponse(assignment);
        } catch (Exception e) {
            log.error("Lỗi khi lấy assignment: {}", e.getMessage(), e);
            throw new RuntimeException("Không thể lấy assignment: " + e.getMessage());
        }
    }

    @Override
    public List<AssignmentResponse> getAssignmentsByTopicId(Integer topicId) {
        try {
            log.info("Lấy assignments theo topicId: {}", topicId);
            
            List<Assignment> assignments = assignmentRepository.findByTopicId(topicId);
            
            return assignments.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Lỗi khi lấy assignments theo topicId: {}", e.getMessage(), e);
            throw new RuntimeException("Không thể lấy assignments: " + e.getMessage());
        }
    }

    @Override
    public List<AssignmentResponse> getAssignmentsByAssignedTo(Integer assignedTo) {
        try {
            log.info("Lấy assignments theo assignedTo: {}", assignedTo);
            
            List<Assignment> assignments = assignmentRepository.findByAssignedTo(assignedTo);
            
            return assignments.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Lỗi khi lấy assignments theo assignedTo: {}", e.getMessage(), e);
            throw new RuntimeException("Không thể lấy assignments: " + e.getMessage());
        }
    }

    @Override
    public List<AssignmentResponse> getAssignmentsByAssignedBy(Integer assignedBy) {
        try {
            log.info("Lấy assignments theo assignedBy: {}", assignedBy);
            
            List<Assignment> assignments = assignmentRepository.findByAssignedBy(assignedBy);
            
            return assignments.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Lỗi khi lấy assignments theo assignedBy: {}", e.getMessage(), e);
            throw new RuntimeException("Không thể lấy assignments: " + e.getMessage());
        }
    }

    @Override
    public boolean deleteAssignment(Integer assignmentId) {
        try {
            log.info("Xóa assignment với ID: {}", assignmentId);
            
            if (!assignmentRepository.existsById(assignmentId)) {
                log.warn("Assignment với ID {} không tồn tại", assignmentId);
                return false;
            }
            
            assignmentRepository.deleteById(assignmentId);
            log.info("Đã xóa assignment thành công với ID: {}", assignmentId);
            
            return true;
        } catch (Exception e) {
            log.error("Lỗi khi xóa assignment: {}", e.getMessage(), e);
            throw new RuntimeException("Không thể xóa assignment: " + e.getMessage());
        }
    }

    @Override
    public AssignmentResponse updateAssignmentStatus(Integer assignmentId, Integer status) {
        try {
            log.info("Cập nhật trạng thái assignment với ID: {} thành status: {}", assignmentId, status);
            
            Assignment assignment = assignmentRepository.findById(assignmentId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy assignment với ID: " + assignmentId));
            
            assignment.setStatus(status);
            assignment.setUpdatedAt(Instant.now());
            
            // Nếu status là completed, set completedAt
            if (status == 3) { // Giả sử 3 là completed
                assignment.setCompletedAt(Instant.now());
            }
            
            Assignment updatedAssignment = assignmentRepository.save(assignment);
            log.info("Đã cập nhật trạng thái assignment thành công với ID: {}", assignmentId);
            
            return convertToResponse(updatedAssignment);
        } catch (Exception e) {
            log.error("Lỗi khi cập nhật trạng thái assignment: {}", e.getMessage(), e);
            throw new RuntimeException("Không thể cập nhật trạng thái assignment: " + e.getMessage());
        }
    }

    @Override
    public TaskResponse createTask(Integer assignmentId, CreateTaskRequest request) {
        try {
            log.info("Tạo task mới cho assignmentId: {}", assignmentId);

            Assignment assignment = assignmentRepository.findById(assignmentId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy assignment với ID: " + assignmentId));

            Task task = new Task();
            task.setAssignment(assignment);
            task.setTopicId(assignment.getTopicId());
            task.setAssignedBy(assignment.getAssignedBy());
            task.setAssignedTo(request.getAssignedTo() != null ? request.getAssignedTo() : assignment.getAssignedTo());
            task.setTaskName(request.getTaskName());
            task.setDescription(request.getDescription());
            task.setStartDate(request.getStartDate());
            task.setEndDate(request.getEndDate());
            task.setPriority(request.getPriority());
            task.setStatus(request.getStatus() != null ? request.getStatus() : 1);
            task.setProgress(request.getProgress() != null ? request.getProgress() : 0f);
            task.setCreatedAt(java.time.Instant.now());
            task.setUpdatedAt(java.time.Instant.now());

            Task saved = taskRepository.save(task);
            log.info("Đã tạo task thành công với ID: {}", saved.getTaskId());
            return convertTaskToResponse(saved);
        } catch (Exception e) {
            log.error("Lỗi khi tạo task: {}", e.getMessage(), e);
            throw new RuntimeException("Không thể tạo task: " + e.getMessage());
        }
    }

    @Override
    public TaskResponse updateTask(Integer taskId, UpdateTaskRequest request) {
        try {
            log.info("Cập nhật task với ID: {}", taskId);
            Task task = taskRepository.findById(taskId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy task với ID: " + taskId));

            if (request.getAssignedTo() != null) task.setAssignedTo(request.getAssignedTo());
            if (request.getTaskName() != null) task.setTaskName(request.getTaskName());
            if (request.getDescription() != null) task.setDescription(request.getDescription());
            if (request.getStartDate() != null) task.setStartDate(request.getStartDate());
            if (request.getEndDate() != null) task.setEndDate(request.getEndDate());
            if (request.getPriority() != null) task.setPriority(request.getPriority());
            if (request.getStatus() != null) task.setStatus(request.getStatus());
            if (request.getProgress() != null) task.setProgress(request.getProgress());

            task.setUpdatedAt(java.time.Instant.now());

            Task saved = taskRepository.save(task);
            return convertTaskToResponse(saved);
        } catch (Exception e) {
            log.error("Lỗi khi cập nhật task: {}", e.getMessage(), e);
            throw new RuntimeException("Không thể cập nhật task: " + e.getMessage());
        }
    }

    @Override
    public boolean deleteTask(Integer taskId) {
        try {
            log.info("Xoá task với ID: {}", taskId);
            if (!taskRepository.existsById(taskId)) {
                return false;
            }
            taskRepository.deleteById(taskId);
            return true;
        } catch (Exception e) {
            log.error("Lỗi khi xoá task: {}", e.getMessage(), e);
            throw new RuntimeException("Không thể xoá task: " + e.getMessage());
        }
    }

    /**
     * Chuyển đổi Assignment entity thành AssignmentResponse
     */
    private AssignmentResponse convertToResponse(Assignment assignment) {
        AssignmentResponse response = new AssignmentResponse();
        response.setAssignmentId(assignment.getAssignmentId());
        response.setTopicId(assignment.getTopicId());
        response.setAssignedTo(assignment.getAssignedTo());
        response.setAssignedBy(assignment.getAssignedBy());
        response.setTitle(assignment.getTitle());
        response.setDescription(assignment.getDescription());
        response.setDueDate(assignment.getDueDate());
        response.setPriority(assignment.getPriority());
        response.setStatus(assignment.getStatus());
        response.setCreatedAt(assignment.getCreatedAt());
        response.setUpdatedAt(assignment.getUpdatedAt());
        response.setCompletedAt(assignment.getCompletedAt());
        
        // Lấy tasks nếu có
        if (assignment.getTasks() != null && !assignment.getTasks().isEmpty()) {
            List<TaskResponse> taskResponses = assignment.getTasks().stream()
                    .map(this::convertTaskToResponse)
                    .collect(Collectors.toList());
            response.setTasks(taskResponses);
        }
        
        return response;
    }

    /**
     * Chuyển đổi Task entity thành TaskResponse
     */
    private TaskResponse convertTaskToResponse(Task task) {
        TaskResponse response = new TaskResponse();
        response.setTaskId(task.getTaskId());
        response.setTopicId(task.getTopicId());
        response.setAssignmentId(task.getAssignment() != null ? task.getAssignment().getAssignmentId() : null);
        response.setAssignedTo(task.getAssignedTo());
        response.setAssignedBy(task.getAssignedBy());
        response.setTaskName(task.getTaskName());
        response.setDescription(task.getDescription());
        response.setStartDate(task.getStartDate());
        response.setEndDate(task.getEndDate());
        response.setPriority(task.getPriority());
        response.setStatus(task.getStatus());
        response.setProgress(task.getProgress());
        response.setCreatedAt(task.getCreatedAt());
        response.setUpdatedAt(task.getUpdatedAt());
        response.setTaskStatus(task.getTaskStatus());
        
        return response;
    }

    // Statistics methods implementation
    @Override
    public Long getAssignmentCount() {
        return assignmentRepository.count();
    }

    @Override
    public Long getAssignmentCountByStatus(String status) {
        try {
            Integer statusValue = Integer.parseInt(status);
            return assignmentRepository.countByStatus(statusValue);
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    @Override
    public Long getAssignmentCountByUser(Integer userId) {
        return assignmentRepository.countByAssignedTo(userId);
    }

    @Override
    public Long getAssignmentCountByTopic(Integer topicId) {
        return assignmentRepository.countByTopicId(topicId);
    }

    @Override
    public List<Map<String, Object>> getAssignmentsByUser(Integer userId) {
        return assignmentRepository.findByAssignedTo(userId).stream()
                .map(assignment -> {
                    Map<String, Object> assignmentMap = new HashMap<>();
                    assignmentMap.put("assignmentId", assignment.getAssignmentId());
                    assignmentMap.put("topicId", assignment.getTopicId());
                    assignmentMap.put("assignedTo", assignment.getAssignedTo());
                    assignmentMap.put("assignedBy", assignment.getAssignedBy());
                    assignmentMap.put("title", assignment.getTitle());
                    assignmentMap.put("description", assignment.getDescription());
                    assignmentMap.put("status", assignment.getStatus());
                    assignmentMap.put("createdAt", assignment.getCreatedAt());
                    return assignmentMap;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getAssignmentsByTopic(Integer topicId) {
        return assignmentRepository.findByTopicId(topicId).stream()
                .map(assignment -> {
                    Map<String, Object> assignmentMap = new HashMap<>();
                    assignmentMap.put("assignmentId", assignment.getAssignmentId());
                    assignmentMap.put("topicId", assignment.getTopicId());
                    assignmentMap.put("assignedTo", assignment.getAssignedTo());
                    assignmentMap.put("assignedBy", assignment.getAssignedBy());
                    assignmentMap.put("title", assignment.getTitle());
                    assignmentMap.put("description", assignment.getDescription());
                    assignmentMap.put("status", assignment.getStatus());
                    assignmentMap.put("createdAt", assignment.getCreatedAt());
                    return assignmentMap;
                })
                .collect(Collectors.toList());
    }

    @Override
    public Long getTaskCount() {
        return taskRepository.count();
    }

    @Override
    public Long getTaskCountByStatus(String status) {
        try {
            Integer statusValue = Integer.parseInt(status);
            return taskRepository.countByStatus(statusValue);
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    @Override
    public Long getTaskCountByAssignment(Integer assignmentId) {
        return taskRepository.countByAssignmentAssignmentId(assignmentId);
    }

    @Override
    public List<Map<String, Object>> getTasksByAssignment(Integer assignmentId) {
        return taskRepository.findByAssignmentAssignmentId(assignmentId).stream()
                .map(task -> {
                    Map<String, Object> taskMap = new HashMap<>();
                    taskMap.put("taskId", task.getTaskId());
                    taskMap.put("assignmentId", task.getAssignment().getAssignmentId());
                    taskMap.put("topicId", task.getTopicId());
                    taskMap.put("assignedTo", task.getAssignedTo());
                    taskMap.put("assignedBy", task.getAssignedBy());
                    taskMap.put("taskName", task.getTaskName());
                    taskMap.put("description", task.getDescription());
                    taskMap.put("status", task.getStatus());
                    taskMap.put("progress", task.getProgress());
                    taskMap.put("createdAt", task.getCreatedAt());
                    return taskMap;
                })
                .collect(Collectors.toList());
    }
}
