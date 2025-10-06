package com.phenikaa.evalservice.repository;

import com.phenikaa.evalservice.entity.CouncilSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CouncilSummaryRepository extends JpaRepository<CouncilSummary, Integer> {
    Optional<CouncilSummary> findByTopicId(Integer topicId);
}


