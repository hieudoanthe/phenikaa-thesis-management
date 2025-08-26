package com.phenikaa.assignservice.repository;

import com.phenikaa.assignservice.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Integer> {
    
    /**
     * Tìm tất cả tasks theo assignmentId
     */
    List<Task> findByAssignment_AssignmentId(Integer assignmentId);
    
    /**
     * Tìm tasks theo topicId
     */
    List<Task> findByTopicId(Integer topicId);
    
    /**
     * Tìm tasks theo người được phân công
     */
    List<Task> findByAssignedTo(Integer assignedTo);
    
    /**
     * Tìm tasks theo assignmentId và status
     */
    List<Task> findByAssignment_AssignmentIdAndStatus(Integer assignmentId, Integer status);
    
    /**
     * Đếm số tasks theo assignmentId
     */
    long countByAssignment_AssignmentId(Integer assignmentId);
}
