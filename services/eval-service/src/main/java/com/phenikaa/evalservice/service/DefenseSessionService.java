package com.phenikaa.evalservice.service;

import com.phenikaa.evalservice.dto.DefenseSessionDto;
import com.phenikaa.evalservice.entity.DefenseSession;
import com.phenikaa.evalservice.entity.DefenseSchedule;
import com.phenikaa.evalservice.entity.DefenseCommittee;
import com.phenikaa.evalservice.repository.DefenseSessionRepository;
import com.phenikaa.evalservice.repository.DefenseScheduleRepository;
import com.phenikaa.evalservice.repository.DefenseCommitteeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DefenseSessionService {

    private final DefenseSessionRepository defenseSessionRepository;
    private final DefenseScheduleRepository defenseScheduleRepository;
    private final DefenseCommitteeRepository defenseCommitteeRepository;

    /**
     * Tạo buổi bảo vệ mới
     */
    public DefenseSessionDto createSession(DefenseSessionDto sessionDto) {
        log.info("Tạo buổi bảo vệ mới: {}", sessionDto.getSessionName());
        
        DefenseSchedule schedule = defenseScheduleRepository.findById(sessionDto.getScheduleId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lịch bảo vệ với ID: " + sessionDto.getScheduleId()));

        DefenseSession session = DefenseSession.builder()
                .sessionName(sessionDto.getSessionName())
                .defenseSchedule(schedule)
                .defenseDate(sessionDto.getDefenseDate())
                .startTime(sessionDto.getStartTime())
                .endTime(sessionDto.getEndTime())
                .location(sessionDto.getLocation())
                .maxStudents(sessionDto.getMaxStudents())
                .status(DefenseSession.SessionStatus.PLANNING)
                .notes(sessionDto.getNotes())
                .build();

        DefenseSession savedSession = defenseSessionRepository.save(session);
        log.info("Đã tạo buổi bảo vệ với ID: {}", savedSession.getSessionId());
        
        // Tạo hội đồng chấm điểm nếu có danh sách thành viên
        if (sessionDto.getCommitteeMembers() != null && !sessionDto.getCommitteeMembers().isEmpty()) {
            createDefenseCommittees(savedSession, sessionDto.getCommitteeMembers(), DefenseCommittee.CommitteeRole.MEMBER);
            log.info("Đã tạo {} thành viên hội đồng cho buổi bảo vệ {}", 
                    sessionDto.getCommitteeMembers().size(), savedSession.getSessionId());
        }
        
        // Tạo giảng viên phản biện nếu có danh sách
        if (sessionDto.getReviewerMembers() != null && !sessionDto.getReviewerMembers().isEmpty()) {
            createDefenseCommittees(savedSession, sessionDto.getReviewerMembers(), DefenseCommittee.CommitteeRole.REVIEWER);
            log.info("Đã tạo {} giảng viên phản biện cho buổi bảo vệ {}", 
                    sessionDto.getReviewerMembers().size(), savedSession.getSessionId());
        }
        
        return DefenseSessionDto.fromEntity(savedSession);
    }

    /**
     * Cập nhật buổi bảo vệ
     */
    public DefenseSessionDto updateSession(Integer sessionId, DefenseSessionDto sessionDto) {
        log.info("Cập nhật buổi bảo vệ ID: {}", sessionId);
        
        DefenseSession existingSession = defenseSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy buổi bảo vệ với ID: " + sessionId));

        existingSession.setSessionName(sessionDto.getSessionName());
        existingSession.setDefenseDate(sessionDto.getDefenseDate());
        existingSession.setStartTime(sessionDto.getStartTime());
        existingSession.setEndTime(sessionDto.getEndTime());
        existingSession.setLocation(sessionDto.getLocation());
        existingSession.setMaxStudents(sessionDto.getMaxStudents());
        existingSession.setNotes(sessionDto.getNotes());

        DefenseSession updatedSession = defenseSessionRepository.save(existingSession);
        log.info("Đã cập nhật buổi bảo vệ ID: {}", sessionId);
        
        return DefenseSessionDto.fromEntity(updatedSession);
    }

    /**
     * Lấy tất cả buổi bảo vệ
     */
    public List<DefenseSessionDto> getAllSessions() {
        List<DefenseSession> sessions = defenseSessionRepository.findAll();
        return sessions.stream()
                .map(DefenseSessionDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Lấy buổi bảo vệ theo ID
     */
    public DefenseSessionDto getSessionById(Integer sessionId) {
        DefenseSession session = defenseSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy buổi bảo vệ với ID: " + sessionId));
        
        return DefenseSessionDto.fromEntity(session);
    }

    /**
     * Lấy tất cả buổi bảo vệ theo lịch
     */
    public List<DefenseSessionDto> getSessionsBySchedule(Integer scheduleId) {
        List<DefenseSession> sessions = defenseSessionRepository.findByDefenseSchedule_ScheduleId(scheduleId);
        return sessions.stream()
                .map(DefenseSessionDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Lấy buổi bảo vệ theo ngày
     */
    public List<DefenseSessionDto> getSessionsByDate(LocalDate date) {
        List<DefenseSession> sessions = defenseSessionRepository.findByDefenseDate(date);
        return sessions.stream()
                .map(DefenseSessionDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Lấy buổi bảo vệ theo khoảng thời gian
     */
    public List<DefenseSessionDto> getSessionsByDateRange(LocalDate startDate, LocalDate endDate) {
        List<DefenseSession> sessions = defenseSessionRepository.findByDateRange(startDate, endDate);
        return sessions.stream()
                .map(DefenseSessionDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Lấy buổi bảo vệ theo trạng thái
     */
    public List<DefenseSessionDto> getSessionsByStatus(DefenseSession.SessionStatus status) {
        List<DefenseSession> sessions = defenseSessionRepository.findByStatus(status);
        return sessions.stream()
                .map(DefenseSessionDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Lấy các buổi bảo vệ có thể thêm sinh viên
     */
    public List<DefenseSessionDto> getAvailableSessions() {
        List<DefenseSession> sessions = defenseSessionRepository.findAvailableSessions();
        return sessions.stream()
                .map(DefenseSessionDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Cập nhật trạng thái buổi bảo vệ
     */
    public void updateSessionStatus(Integer sessionId, DefenseSession.SessionStatus status) {
        log.info("Cập nhật trạng thái buổi bảo vệ ID: {} thành {}", sessionId, status);
        
        DefenseSession session = defenseSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy buổi bảo vệ với ID: " + sessionId));

        session.setStatus(status);
        defenseSessionRepository.save(session);
        
        log.info("Đã cập nhật trạng thái buổi bảo vệ ID: {}", sessionId);
    }

    /**
     * Xóa buổi bảo vệ
     */
    public void deleteSession(Integer sessionId) {
        log.info("Xóa buổi bảo vệ ID: {}", sessionId);
        
        DefenseSession session = defenseSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy buổi bảo vệ với ID: " + sessionId));

        // Kiểm tra xem có thể xóa không
        if (session.getStatus() == DefenseSession.SessionStatus.IN_PROGRESS || 
            session.getStatus() == DefenseSession.SessionStatus.COMPLETED) {
            throw new RuntimeException("Không thể xóa buổi bảo vệ đang diễn ra hoặc đã hoàn thành");
        }

        defenseSessionRepository.delete(session);
        log.info("Đã xóa buổi bảo vệ ID: {}", sessionId);
    }

    /**
     * Lấy buổi bảo vệ theo địa điểm
     */
    public List<DefenseSessionDto> getSessionsByLocation(String location) {
        List<DefenseSession> sessions = defenseSessionRepository.findByLocation(location);
        return sessions.stream()
                .map(DefenseSessionDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Tạo hội đồng chấm điểm cho buổi bảo vệ
     */
    private void createDefenseCommittees(DefenseSession session, List<Integer> lecturerIds, DefenseCommittee.CommitteeRole role) {
        for (Integer lecturerId : lecturerIds) {
            DefenseCommittee committee = DefenseCommittee.builder()
                    .defenseSession(session)
                    .lecturerId(lecturerId)
                    .role(role)
                    .build();
            
            defenseCommitteeRepository.save(committee);
            log.info("Đã tạo thành viên hội đồng: lecturerId={}, sessionId={}, role={}", 
                    lecturerId, session.getSessionId(), role);
        }
    }
}
