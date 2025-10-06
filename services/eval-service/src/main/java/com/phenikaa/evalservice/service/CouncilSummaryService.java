package com.phenikaa.evalservice.service;

import com.phenikaa.evalservice.entity.CouncilSummary;
import com.phenikaa.evalservice.entity.DefenseCommittee;
import com.phenikaa.evalservice.repository.CouncilSummaryRepository;
import com.phenikaa.evalservice.repository.DefenseCommitteeRepository;
import com.phenikaa.evalservice.repository.StudentDefenseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CouncilSummaryService {

    private final CouncilSummaryRepository repository;
    private final StudentDefenseRepository studentDefenseRepository;
    private final DefenseCommitteeRepository defenseCommitteeRepository;

    public CouncilSummary upsert(Integer topicId, Integer chairmanId, String content) {
        Optional<CouncilSummary> existing = repository.findByTopicId(topicId);
        CouncilSummary summary = existing.orElseGet(() -> CouncilSummary.builder()
                .topicId(topicId)
                .build());
        summary.setChairmanId(chairmanId);
        summary.setContent(content);
        return repository.save(summary);
    }

    public Optional<CouncilSummary> getByTopicId(Integer topicId) {
        return repository.findByTopicId(topicId);
    }

    public boolean hasChairmanAccess(Integer lecturerId, Integer topicId) {
        var sd = studentDefenseRepository.findWithSessionByTopicId(topicId).orElse(null);
        if (sd == null || sd.getDefenseSession() == null) {
            log.warn("No StudentDefense/DefenseSession for topicId={}", topicId);
            return false;
        }
        var sessionId = sd.getDefenseSession().getSessionId();
        var committee = defenseCommitteeRepository.findByDefenseSession_SessionIdAndRole(
                sessionId, DefenseCommittee.CommitteeRole.CHAIRMAN);
        boolean isChairman = committee.isPresent() && committee.get().getLecturerId().equals(lecturerId);
        log.info("Chairman access check: lecturerId={}, topicId={}, sessionId={}, isChairman={}",
                lecturerId, topicId, sessionId, isChairman);
        return isChairman;
    }
}


