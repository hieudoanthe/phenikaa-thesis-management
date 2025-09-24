package com.phenikaa.evalservice.controller;

import com.phenikaa.evalservice.entity.DefenseCommittee;
import com.phenikaa.evalservice.entity.DefenseSession;
import com.phenikaa.evalservice.repository.DefenseCommitteeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/eval-service/teacher/schedule")
@RequiredArgsConstructor
@Slf4j
public class TeacherScheduleController {

    private final DefenseCommitteeRepository defenseCommitteeRepository;

    /**
     * Trả về danh sách các DefenseSession mà giảng viên tham gia
     */
    @GetMapping("/evaluator/{evaluatorId}/sessions")
    public ResponseEntity<List<Map<String, Object>>> getSessionsByLecturer(@PathVariable Integer evaluatorId) {
        try {
            List<DefenseCommittee> committees = defenseCommitteeRepository.findByLecturerId(evaluatorId);

            // Gom session distinct
            Set<DefenseSession> sessions = committees.stream()
                    .map(DefenseCommittee::getDefenseSession)
                    .filter(java.util.Objects::nonNull)
                    .collect(Collectors.toSet());

            // Map session -> các vai trò của giảng viên trong session đó
            List<Map<String, Object>> result = sessions.stream()
                    .sorted(Comparator.comparing(DefenseSession::getDefenseDate))
                    .map(session -> {
                        List<String> roles = committees.stream()
                                .filter(dc -> dc.getDefenseSession() != null &&
                                        dc.getDefenseSession().getSessionId().equals(session.getSessionId()))
                                .map(dc -> dc.getRole() != null ? dc.getRole().name() : "MEMBER")
                                .distinct()
                                .collect(Collectors.toList());

                        return Map.of(
                                "sessionId", session.getSessionId(),
                                "sessionName", session.getSessionName(),
                                "defenseDate", session.getDefenseDate(),
                                "startTime", session.getStartTime(),
                                "endTime", session.getEndTime(),
                                "location", session.getLocation(),
                                "status", session.getStatus() != null ? session.getStatus().name() : null,
                                "roles", roles
                        );
                    })
                    .collect(Collectors.toList());

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error fetching sessions for evaluator {}: {}", evaluatorId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}


