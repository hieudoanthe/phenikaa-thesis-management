package com.phenikaa.evalservice.service;

import com.phenikaa.evalservice.entity.SupervisorSummary;
import com.phenikaa.evalservice.repository.StudentDefenseRepository;
import com.phenikaa.evalservice.repository.SupervisorSummaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SupervisorSummaryService {
    private final SupervisorSummaryRepository repository;
    private final StudentDefenseRepository studentDefenseRepository;

    public Optional<SupervisorSummary> getByTopicId(Integer topicId) {
        return repository.findByTopicId(topicId);
    }

    public SupervisorSummary upsert(Integer topicId, Integer supervisorId, String content) {
        SupervisorSummary entity = repository.findByTopicId(topicId).orElseGet(SupervisorSummary::new);
        entity.setTopicId(topicId);
        entity.setSupervisorId(supervisorId);
        entity.setContent(content);
        return repository.save(entity);
    }

    public boolean hasSupervisorAccess(Integer lecturerId, Integer topicId) {
        var sd = studentDefenseRepository.findWithSessionByTopicId(topicId).orElse(null);
        if (sd == null) return false;
        return sd.getSupervisorId() != null && sd.getSupervisorId().equals(lecturerId);
    }
}
