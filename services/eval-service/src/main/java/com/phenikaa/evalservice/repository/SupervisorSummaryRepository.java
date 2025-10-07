package com.phenikaa.evalservice.repository;

import com.phenikaa.evalservice.entity.SupervisorSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SupervisorSummaryRepository extends JpaRepository<SupervisorSummary, Integer> {
    Optional<SupervisorSummary> findByTopicId(Integer topicId);
}
