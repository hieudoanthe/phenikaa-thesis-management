package com.phenikaa.thesisservice.repository;

import com.phenikaa.thesisservice.entity.SuggestedTopic;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SuggestRepository extends JpaRepository<SuggestedTopic, Integer> {
}
