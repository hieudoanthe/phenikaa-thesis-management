package com.phenikaa.assignservice.repository;

import com.phenikaa.assignservice.entity.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Integer> {
    
    /**
     * Tìm tất cả assignments theo topicId
     */
    List<Assignment> findByTopicId(Integer topicId);
    
    /**
     * Tìm assignments theo người được phân công
     */
    List<Assignment> findByAssignedTo(Integer assignedTo);
    
    /**
     * Tìm assignments theo người phân công
     */
    List<Assignment> findByAssignedBy(Integer assignedBy);
    
    /**
     * Tìm assignments theo topicId và status
     */
    List<Assignment> findByTopicIdAndStatus(Integer topicId, Integer status);
    
    /**
     * Kiểm tra xem topic có assignment nào không
     */
    boolean existsByTopicId(Integer topicId);
    
    /**
     * Đếm số assignments theo topicId
     */
    long countByTopicId(Integer topicId);
}
