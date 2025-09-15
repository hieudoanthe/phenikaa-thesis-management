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

    Page<ProjectTopicSummary> findSummariesBySupervisorIdAndApprovalStatus(Integer supervisorId, ProjectTopic.ApprovalStatus approvalStatus, Pageable pageable);

    @EntityGraph(attributePaths = {"suggestedTopics", "registers"})
    Page<ProjectTopic> findBySupervisorIdAndApprovalStatus(
            Integer supervisorId, 
            ProjectTopic.ApprovalStatus approvalStatus, 
            Pageable pageable
    );

    @Query("select new com.phenikaa.thesisservice.dto.response.ProjectTopicSummaryDto(p.topicId, p.topicCode, p.title, p.difficultyLevel, p.approvalStatus) " +
           "from ProjectTopic p where p.supervisorId = :supervisorId")
    Page<ProjectTopicSummaryDto> findSummaryDtosBySupervisorId(@Param("supervisorId") Integer supervisorId, Pageable pageable);

    <T> Page<T> findByApprovalStatus(ProjectTopic.ApprovalStatus status, Class<T> type, Pageable pageable);

    @Query("SELECT COUNT(p) FROM ProjectTopic p WHERE p.supervisorId = ?1 AND p.approvalStatus = 'APPROVED'")
    Long countApprovedTopicsBySupervisor(Integer supervisorId);
    
    // Lấy tất cả đề tài có trạng thái APPROVED với phân trang
    @EntityGraph(attributePaths = {"suggestedTopics", "registers"})
    Page<ProjectTopic> findByApprovalStatus(ProjectTopic.ApprovalStatus approvalStatus, Pageable pageable);
    
    // Statistics methods
    Long countByTopicStatus(ProjectTopic.TopicStatus topicStatus);
    Long countByDifficultyLevel(ProjectTopic.DifficultyLevel difficultyLevel);
    Long countByAcademicYearId(Integer academicYearId);
    Long countBySupervisorId(Integer supervisorId);
    List<ProjectTopic> findBySupervisorId(Integer supervisorId);
    Long countBySupervisorIdAndTopicStatus(Integer supervisorId, ProjectTopic.TopicStatus topicStatus);
    Long countBySupervisorIdAndDifficultyLevel(Integer supervisorId, ProjectTopic.DifficultyLevel difficultyLevel);
}
