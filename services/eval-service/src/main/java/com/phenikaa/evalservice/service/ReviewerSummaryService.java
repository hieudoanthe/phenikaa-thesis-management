package com.phenikaa.evalservice.service;

import com.phenikaa.evalservice.entity.DefenseCommittee;
import com.phenikaa.evalservice.entity.ReviewerSummary;
import com.phenikaa.evalservice.repository.DefenseCommitteeRepository;
import com.phenikaa.evalservice.repository.ReviewerSummaryRepository;
import com.phenikaa.evalservice.repository.StudentDefenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReviewerSummaryService {
    private final ReviewerSummaryRepository repository;
    private final StudentDefenseRepository studentDefenseRepository;
    private final DefenseCommitteeRepository defenseCommitteeRepository;

    public Optional<ReviewerSummary> getByTopicId(Integer topicId) {
        return repository.findByTopicId(topicId);
    }

    public ReviewerSummary upsert(Integer topicId, Integer reviewerId, String content) {
        ReviewerSummary entity = repository.findByTopicId(topicId).orElseGet(ReviewerSummary::new);
        entity.setTopicId(topicId);
        entity.setReviewerId(reviewerId);
        entity.setContent(content);
        return repository.save(entity);
    }

    public boolean hasReviewerAccess(Integer lecturerId, Integer topicId) {
        var sd = studentDefenseRepository.findWithSessionByTopicId(topicId).orElse(null);
        if (sd == null || sd.getDefenseSession() == null) return false;
        var sessionId = sd.getDefenseSession().getSessionId();
        var committee = defenseCommitteeRepository.findByDefenseSession_SessionId(sessionId);
        if (committee == null) return false;
        return committee.stream().anyMatch(dc -> dc.getRole() == DefenseCommittee.CommitteeRole.REVIEWER && lecturerId.equals(dc.getLecturerId()));
    }
}
