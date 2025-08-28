package com.phenikaa.thesisservice.repository;

import com.phenikaa.thesisservice.entity.SuggestedTopic;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SuggestRepository extends JpaRepository<SuggestedTopic, Integer> {
    Page<SuggestedTopic> findBySuggestedBy(Integer studentId, Pageable pageable);
}
