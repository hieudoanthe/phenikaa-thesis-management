package com.phenikaa.thesisservice.repository;

import com.phenikaa.thesisservice.entity.ProjectTopic;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectTopicRepository extends JpaRepository <ProjectTopic, Integer>{
    List<ProjectTopic> findByApprovalStatusAndTopicStatus(
            ProjectTopic.ApprovalStatus approvalStatus,
            ProjectTopic.TopicStatus topicStatus
    );
    List<ProjectTopic> findBySupervisorId(Integer userId);
    Page<ProjectTopic> findBySupervisorId(Integer supervisorId, Pageable pageable);
}
