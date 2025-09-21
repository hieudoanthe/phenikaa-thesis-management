package com.phenikaa.evalservice.service;

import com.phenikaa.evalservice.dto.DefenseSessionDto;
import com.phenikaa.evalservice.entity.DefenseSession;
import com.phenikaa.evalservice.entity.DefenseSchedule;
import com.phenikaa.evalservice.entity.DefenseCommittee;
import com.phenikaa.evalservice.exception.DefenseSessionValidationException;
import com.phenikaa.evalservice.exception.DefenseSessionNotFoundException;
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

    private static final String SESSION_NOT_FOUND_MESSAGE = "Không tìm thấy buổi bảo vệ với ID: ";
    
    private final DefenseSessionRepository defenseSessionRepository;
    private final DefenseScheduleRepository defenseScheduleRepository;
    private final DefenseCommitteeRepository defenseCommitteeRepository;

    /**
     * Tạo buổi bảo vệ mới
     */
    public DefenseSessionDto createSession(DefenseSessionDto sessionDto) {
        log.info("Tạo buổi bảo vệ mới: {}", sessionDto.getSessionName());
        
        // Validate dữ liệu đầu vào
        validateSessionData(sessionDto);
        
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
            createDefenseCommitteesWithRoles(savedSession, sessionDto.getCommitteeMembers());
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
                .orElseThrow(() -> new DefenseSessionNotFoundException(SESSION_NOT_FOUND_MESSAGE + sessionId));

        // Validate dữ liệu đầu vào (loại trừ session hiện tại khỏi kiểm tra xung đột)
        validateSessionDataForUpdate(sessionDto, sessionId);

        existingSession.setSessionName(sessionDto.getSessionName());
        existingSession.setDefenseDate(sessionDto.getDefenseDate());
        existingSession.setStartTime(sessionDto.getStartTime());
        existingSession.setEndTime(sessionDto.getEndTime());
        existingSession.setLocation(sessionDto.getLocation());
        existingSession.setMaxStudents(sessionDto.getMaxStudents());
        existingSession.setNotes(sessionDto.getNotes());

        DefenseSession updatedSession = defenseSessionRepository.save(existingSession);
        log.info("Đã cập nhật buổi bảo vệ ID: {}", sessionId);
        
        // Cập nhật hội đồng chấm điểm nếu có danh sách thành viên mới
        if (sessionDto.getCommitteeMembers() != null && !sessionDto.getCommitteeMembers().isEmpty()) {
            // Xóa hội đồng cũ
            List<DefenseCommittee> existingCommittees = defenseCommitteeRepository.findByDefenseSession_SessionId(sessionId);
            for (DefenseCommittee committee : existingCommittees) {
                if (committee.getRole() == DefenseCommittee.CommitteeRole.CHAIRMAN || 
                    committee.getRole() == DefenseCommittee.CommitteeRole.SECRETARY || 
                    committee.getRole() == DefenseCommittee.CommitteeRole.MEMBER) {
                    defenseCommitteeRepository.delete(committee);
                }
            }
            
            // Tạo hội đồng mới với vai trò theo thứ tự
            createDefenseCommitteesWithRoles(updatedSession, sessionDto.getCommitteeMembers());
            log.info("Đã cập nhật {} thành viên hội đồng cho buổi bảo vệ {}", 
                    sessionDto.getCommitteeMembers().size(), sessionId);
        }
        
        // Cập nhật giảng viên phản biện nếu có danh sách mới
        if (sessionDto.getReviewerMembers() != null && !sessionDto.getReviewerMembers().isEmpty()) {
            // Xóa giảng viên phản biện cũ
            List<DefenseCommittee> existingCommittees = defenseCommitteeRepository.findByDefenseSession_SessionId(sessionId);
            for (DefenseCommittee committee : existingCommittees) {
                if (committee.getRole() == DefenseCommittee.CommitteeRole.REVIEWER) {
                    defenseCommitteeRepository.delete(committee);
                }
            }
            
            // Tạo giảng viên phản biện mới
            createDefenseCommittees(updatedSession, sessionDto.getReviewerMembers(), DefenseCommittee.CommitteeRole.REVIEWER);
            log.info("Đã cập nhật {} giảng viên phản biện cho buổi bảo vệ {}", 
                    sessionDto.getReviewerMembers().size(), sessionId);
        }
        
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
                .orElseThrow(() -> new DefenseSessionNotFoundException(SESSION_NOT_FOUND_MESSAGE + sessionId));
        
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
                .orElseThrow(() -> new DefenseSessionNotFoundException(SESSION_NOT_FOUND_MESSAGE + sessionId));

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
                .orElseThrow(() -> new DefenseSessionNotFoundException(SESSION_NOT_FOUND_MESSAGE + sessionId));

        // Kiểm tra xem có thể xóa không
        if (session.getStatus() == DefenseSession.SessionStatus.IN_PROGRESS || 
            session.getStatus() == DefenseSession.SessionStatus.COMPLETED) {
            throw new DefenseSessionValidationException("Không thể xóa buổi bảo vệ đang diễn ra hoặc đã hoàn thành");
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

    /**
     * Tạo hội đồng chấm điểm với vai trò theo thứ tự
     * Thứ tự 1: Chủ tịch hội đồng, Thứ tự 2: Thư ký, Thứ tự 3: Thành viên
     */
    private void createDefenseCommitteesWithRoles(DefenseSession session, List<Integer> lecturerIds) {
        for (int i = 0; i < lecturerIds.size(); i++) {
            DefenseCommittee.CommitteeRole role;
            switch (i) {
                case 0:
                    role = DefenseCommittee.CommitteeRole.CHAIRMAN; // Chủ tịch hội đồng
                    break;
                case 1:
                    role = DefenseCommittee.CommitteeRole.SECRETARY; // Thư ký
                    break;
                default:
                    role = DefenseCommittee.CommitteeRole.MEMBER; // Thành viên
                    break;
            }

            DefenseCommittee committee = DefenseCommittee.builder()
                    .defenseSession(session)
                    .lecturerId(lecturerIds.get(i))
                    .role(role)
                    .build();
            
            defenseCommitteeRepository.save(committee);
            log.info("Đã tạo thành viên hội đồng: lecturerId={}, sessionId={}, role={} (thứ tự {})", 
                    lecturerIds.get(i), session.getSessionId(), role, i + 1);
        }
    }

    /**
     * Validate dữ liệu buổi bảo vệ trước khi tạo
     */
    private void validateSessionData(DefenseSessionDto sessionDto) {
        // Validate số lượng thành viên hội đồng (tối đa 3)
        if (sessionDto.getCommitteeMembers() != null && sessionDto.getCommitteeMembers().size() > 3) {
            throw new DefenseSessionValidationException("Số lượng thành viên hội đồng không được vượt quá 3 người");
        }

        // Validate số lượng giảng viên phản biện (tối đa 1)
        if (sessionDto.getReviewerMembers() != null && sessionDto.getReviewerMembers().size() > 1) {
            throw new DefenseSessionValidationException("Số lượng giảng viên phản biện không được vượt quá 1 người");
        }

        // Validate thời gian
        if (sessionDto.getStartTime() != null && sessionDto.getEndTime() != null && 
            sessionDto.getStartTime().isAfter(sessionDto.getEndTime())) {
            throw new DefenseSessionValidationException("Thời gian bắt đầu không được sau thời gian kết thúc");
        }

        // Validate phòng không trùng cùng thời gian
        validateLocationTimeConflict(sessionDto);

        // Validate giảng viên không trùng lịch
        validateLecturerTimeConflict(sessionDto);
    }

    /**
     * Validate dữ liệu buổi bảo vệ trước khi cập nhật (loại trừ session hiện tại)
     */
    private void validateSessionDataForUpdate(DefenseSessionDto sessionDto, Integer currentSessionId) {
        // Validate số lượng thành viên hội đồng (tối đa 3)
        if (sessionDto.getCommitteeMembers() != null && sessionDto.getCommitteeMembers().size() > 3) {
            throw new DefenseSessionValidationException("Số lượng thành viên hội đồng không được vượt quá 3 người");
        }

        // Validate số lượng giảng viên phản biện (tối đa 1)
        if (sessionDto.getReviewerMembers() != null && sessionDto.getReviewerMembers().size() > 1) {
            throw new DefenseSessionValidationException("Số lượng giảng viên phản biện không được vượt quá 1 người");
        }

        // Validate thời gian
        if (sessionDto.getStartTime() != null && sessionDto.getEndTime() != null && 
            sessionDto.getStartTime().isAfter(sessionDto.getEndTime())) {
            throw new DefenseSessionValidationException("Thời gian bắt đầu không được sau thời gian kết thúc");
        }

        // Validate phòng không trùng cùng thời gian (loại trừ session hiện tại)
        validateLocationTimeConflictForUpdate(sessionDto, currentSessionId);

        // Validate giảng viên không trùng lịch (loại trừ session hiện tại)
        validateLecturerTimeConflictForUpdate(sessionDto, currentSessionId);
    }

    /**
     * Kiểm tra xung đột phòng cùng thời gian
     */
    private void validateLocationTimeConflict(DefenseSessionDto sessionDto) {
        if (sessionDto.getLocation() == null || sessionDto.getStartTime() == null || sessionDto.getEndTime() == null) {
            return;
        }

        List<DefenseSession> conflictingSessions = defenseSessionRepository.findByLocation(sessionDto.getLocation())
                .stream()
                .filter(session -> isTimeOverlap(sessionDto.getStartTime(), sessionDto.getEndTime(), 
                                               session.getStartTime(), session.getEndTime()))
                .toList();

        if (!conflictingSessions.isEmpty()) {
            throw new DefenseSessionValidationException("Phòng " + sessionDto.getLocation() + 
                    " đã được sử dụng trong khoảng thời gian này. Vui lòng chọn phòng khác hoặc thời gian khác.");
        }
    }

    /**
     * Kiểm tra xung đột lịch giảng viên
     */
    private void validateLecturerTimeConflict(DefenseSessionDto sessionDto) {
        if (sessionDto.getStartTime() == null || sessionDto.getEndTime() == null) {
            return;
        }

        // Kiểm tra tất cả giảng viên trong hội đồng
        if (sessionDto.getCommitteeMembers() != null) {
            for (Integer lecturerId : sessionDto.getCommitteeMembers()) {
                validateLecturerTimeConflict(lecturerId, sessionDto.getStartTime(), sessionDto.getEndTime());
            }
        }

        // Kiểm tra giảng viên phản biện
        if (sessionDto.getReviewerMembers() != null) {
            for (Integer lecturerId : sessionDto.getReviewerMembers()) {
                validateLecturerTimeConflict(lecturerId, sessionDto.getStartTime(), sessionDto.getEndTime());
            }
        }
    }

    /**
     * Kiểm tra xung đột lịch của một giảng viên cụ thể
     */
    private void validateLecturerTimeConflict(Integer lecturerId, LocalDateTime startTime, LocalDateTime endTime) {
        List<DefenseCommittee> lecturerCommittees = defenseCommitteeRepository.findByLecturerId(lecturerId);
        
        for (DefenseCommittee committee : lecturerCommittees) {
            DefenseSession existingSession = committee.getDefenseSession();
            if (existingSession != null && 
                isTimeOverlap(startTime, endTime, existingSession.getStartTime(), existingSession.getEndTime())) {
                
                throw new DefenseSessionValidationException("Giảng viên ID " + lecturerId + 
                        " đã có lịch bảo vệ trong khoảng thời gian này. Vui lòng chọn thời gian khác.");
            }
        }
    }

    /**
     * Kiểm tra xung đột phòng cùng thời gian cho update (loại trừ session hiện tại)
     */
    private void validateLocationTimeConflictForUpdate(DefenseSessionDto sessionDto, Integer currentSessionId) {
        if (sessionDto.getLocation() == null || sessionDto.getStartTime() == null || sessionDto.getEndTime() == null) {
            return;
        }

        List<DefenseSession> conflictingSessions = defenseSessionRepository.findByLocation(sessionDto.getLocation())
                .stream()
                .filter(session -> !session.getSessionId().equals(currentSessionId)) // Loại trừ session hiện tại
                .filter(session -> isTimeOverlap(sessionDto.getStartTime(), sessionDto.getEndTime(), 
                                               session.getStartTime(), session.getEndTime()))
                .toList();

        if (!conflictingSessions.isEmpty()) {
            throw new DefenseSessionValidationException("Phòng " + sessionDto.getLocation() + 
                    " đã được sử dụng trong khoảng thời gian này. Vui lòng chọn phòng khác hoặc thời gian khác.");
        }
    }

    /**
     * Kiểm tra xung đột lịch giảng viên cho update (loại trừ session hiện tại)
     */
    private void validateLecturerTimeConflictForUpdate(DefenseSessionDto sessionDto, Integer currentSessionId) {
        if (sessionDto.getStartTime() == null || sessionDto.getEndTime() == null) {
            return;
        }

        // Kiểm tra tất cả giảng viên trong hội đồng
        if (sessionDto.getCommitteeMembers() != null) {
            for (Integer lecturerId : sessionDto.getCommitteeMembers()) {
                validateLecturerTimeConflictForUpdate(lecturerId, sessionDto.getStartTime(), sessionDto.getEndTime(), currentSessionId);
            }
        }

        // Kiểm tra giảng viên phản biện
        if (sessionDto.getReviewerMembers() != null) {
            for (Integer lecturerId : sessionDto.getReviewerMembers()) {
                validateLecturerTimeConflictForUpdate(lecturerId, sessionDto.getStartTime(), sessionDto.getEndTime(), currentSessionId);
            }
        }
    }

    /**
     * Kiểm tra xung đột lịch của một giảng viên cụ thể cho update (loại trừ session hiện tại)
     */
    private void validateLecturerTimeConflictForUpdate(Integer lecturerId, LocalDateTime startTime, LocalDateTime endTime, Integer currentSessionId) {
        List<DefenseCommittee> lecturerCommittees = defenseCommitteeRepository.findByLecturerId(lecturerId);
        
        for (DefenseCommittee committee : lecturerCommittees) {
            DefenseSession existingSession = committee.getDefenseSession();
            if (existingSession != null && 
                !existingSession.getSessionId().equals(currentSessionId) && // Loại trừ session hiện tại
                isTimeOverlap(startTime, endTime, existingSession.getStartTime(), existingSession.getEndTime())) {
                
                throw new DefenseSessionValidationException("Giảng viên ID " + lecturerId + 
                        " đã có lịch bảo vệ trong khoảng thời gian này. Vui lòng chọn thời gian khác.");
            }
        }
    }

    /**
     * Kiểm tra xem hai khoảng thời gian có trùng lặp không
     */
    private boolean isTimeOverlap(LocalDateTime start1, LocalDateTime end1, LocalDateTime start2, LocalDateTime end2) {
        if (start1 == null || end1 == null || start2 == null || end2 == null) {
            return false;
        }
        
        // Hai khoảng thời gian trùng lặp nếu:
        // start1 < end2 AND start2 < end1
        return start1.isBefore(end2) && start2.isBefore(end1);
    }
}
