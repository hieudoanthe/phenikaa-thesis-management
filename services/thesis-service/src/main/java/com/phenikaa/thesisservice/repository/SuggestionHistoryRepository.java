package com.phenikaa.thesisservice.repository;

import com.phenikaa.thesisservice.entity.SuggestionHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SuggestionHistoryRepository extends JpaRepository<SuggestionHistory, Integer> {
    Page<SuggestionHistory> findByStudentIdOrderByCreatedAtDesc(Integer studentId, Pageable pageable);
}


