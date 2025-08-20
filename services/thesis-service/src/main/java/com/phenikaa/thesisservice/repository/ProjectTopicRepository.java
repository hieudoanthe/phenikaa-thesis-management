package com.phenikaa.thesisservice.repository;

import com.phenikaa.thesisservice.entity.ProjectTopic;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;

public interface ProjectTopicRepository extends JpaRepository <ProjectTopic, Integer>{
    
    @Query("SELECT p FROM ProjectTopic p LEFT JOIN FETCH p.suggestedTopics LEFT JOIN FETCH p.registers")
    List<ProjectTopic> findAllWithAssociations();

    List<ProjectTopic> findByApprovalStatusAndTopicStatus(
            ProjectTopic.ApprovalStatus approvalStatus,
            ProjectTopic.TopicStatus topicStatus
    );
    @EntityGraph(attributePaths = {"suggestedTopics", "registers"})
    Page<ProjectTopic> findBySupervisorId(Integer supervisorId, Pageable pageable);
    
}
