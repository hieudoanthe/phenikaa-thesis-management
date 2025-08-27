package com.phenikaa.thesisservice.repository;

import com.phenikaa.thesisservice.entity.ProjectTopic;
import com.phenikaa.thesisservice.projection.ProjectTopicSummary;
import com.phenikaa.thesisservice.dto.response.ProjectTopicSummaryDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProjectTopicRepository extends JpaRepository<ProjectTopic, Integer>, JpaSpecificationExecutor<ProjectTopic> {
    
    @Query("SELECT p FROM ProjectTopic p LEFT JOIN FETCH p.suggestedTopics LEFT JOIN FETCH p.registers")
    List<ProjectTopic> findAllWithAssociations();

    List<ProjectTopic> findByApprovalStatusAndTopicStatus(
            ProjectTopic.ApprovalStatus approvalStatus,
            ProjectTopic.TopicStatus topicStatus
    );
    
    @EntityGraph(attributePaths = {"suggestedTopics", "registers"})
    Page<ProjectTopic> findBySupervisorId(Integer supervisorId, Pageable pageable);
    
    // Interface-based projection: trả ra ProjectTopicSummary (đổi tên tránh trùng chữ ký)
    Page<ProjectTopicSummary> findSummariesBySupervisorIdAndApprovalStatus(Integer supervisorId, ProjectTopic.ApprovalStatus approvalStatus, Pageable pageable);
    
    // Thêm method để đếm theo trạng thái
    @Query("SELECT COUNT(p) FROM ProjectTopic p WHERE p.topicStatus = ?1")
    Long countByTopicStatus(ProjectTopic.TopicStatus topicStatus);
    
    @Query("SELECT COUNT(p) FROM ProjectTopic p WHERE p.approvalStatus = ?1")
    Long countByApprovalStatus(ProjectTopic.ApprovalStatus approvalStatus);
    
    @Query("SELECT COUNT(p) FROM ProjectTopic p WHERE p.topicStatus = ?1 AND p.approvalStatus = ?2")
    Long countByTopicStatusAndApprovalStatus(ProjectTopic.TopicStatus topicStatus, ProjectTopic.ApprovalStatus approvalStatus);
    
    // Thêm method để tìm đề tài đã được xác nhận bởi giảng viên
    @Query("SELECT p FROM ProjectTopic p LEFT JOIN FETCH p.suggestedTopics LEFT JOIN FETCH p.registers " +
           "WHERE p.supervisorId = ?1 AND p.approvalStatus = 'APPROVED'")
    List<ProjectTopic> findApprovedTopicsBySupervisor(Integer supervisorId);
    
    // Thêm method với pagination
    @EntityGraph(attributePaths = {"suggestedTopics", "registers"})
    Page<ProjectTopic> findBySupervisorIdAndApprovalStatus(
            Integer supervisorId, 
            ProjectTopic.ApprovalStatus approvalStatus, 
            Pageable pageable
    );
    
    // Class-based projection (constructor expression)
    @Query("select new com.phenikaa.thesisservice.dto.response.ProjectTopicSummaryDto(p.topicId, p.topicCode, p.title, p.difficultyLevel, p.approvalStatus) " +
           "from ProjectTopic p where p.supervisorId = :supervisorId")
    Page<ProjectTopicSummaryDto> findSummaryDtosBySupervisorId(@Param("supervisorId") Integer supervisorId, Pageable pageable);
    
    // Dynamic projection: method generic với Class<T>
    <T> Page<T> findByApprovalStatus(ProjectTopic.ApprovalStatus status, Class<T> type, Pageable pageable);
    
    // Thêm method để đếm số đề tài đã xác nhận của giảng viên
    @Query("SELECT COUNT(p) FROM ProjectTopic p WHERE p.supervisorId = ?1 AND p.approvalStatus = 'APPROVED'")
    Long countApprovedTopicsBySupervisor(Integer supervisorId);
}
