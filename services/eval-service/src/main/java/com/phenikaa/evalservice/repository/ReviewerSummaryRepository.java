package com.phenikaa.evalservice.repository;

import com.phenikaa.evalservice.entity.ReviewerSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewerSummaryRepository extends JpaRepository<ReviewerSummary, Integer> {
    Optional<ReviewerSummary> findByTopicId(Integer topicId);
}
