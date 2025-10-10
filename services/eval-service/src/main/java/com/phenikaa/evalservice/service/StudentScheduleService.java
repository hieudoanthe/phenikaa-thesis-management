package com.phenikaa.evalservice.service;

import com.phenikaa.evalservice.dto.StudentScheduleDto;
import com.phenikaa.evalservice.entity.DefenseSession;
import com.phenikaa.evalservice.entity.StudentDefense;
import com.phenikaa.evalservice.repository.DefenseSessionRepository;
import com.phenikaa.evalservice.repository.StudentDefenseRepository;
import com.phenikaa.evalservice.client.ThesisServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudentScheduleService {

    private final StudentDefenseRepository studentDefenseRepository;
    private final ThesisServiceClient thesisServiceClient;

    /**
     * Lấy lịch trình đầy đủ của sinh viên
     */
    public List<StudentScheduleDto> getStudentSchedule(Integer studentId) {
        List<StudentScheduleDto> schedule = new ArrayList<>();
        
        // Lấy lịch bảo vệ của sinh viên
        List<StudentDefense> studentDefenses = studentDefenseRepository.findByStudentId(studentId);
        schedule.addAll(studentDefenses.stream()
            .map(this::convertToScheduleDto)
            .collect(Collectors.toList()));

        // Lấy các deadline từ thesis service
        try {
            List<Map<String, Object>> thesisDeadlines = thesisServiceClient.getStudentDeadlines(studentId);
            schedule.addAll(thesisDeadlines.stream()
                .map(deadline -> convertDeadlineToScheduleDto(deadline, studentId))
                .collect(Collectors.toList()));
        } catch (Exception e) {
            log.error("Lỗi khi lấy deadline từ thesis service cho sinh viên {}: {}", studentId, e.getMessage());
        }

        // Sắp xếp theo ngày
        schedule.sort((a, b) -> {
            if (a.getDate().equals(b.getDate())) {
                if (a.getTime() == null && b.getTime() == null) return 0;
                if (a.getTime() == null) return 1;
                if (b.getTime() == null) return -1;
                return a.getTime().compareTo(b.getTime());
            }
            return a.getDate().compareTo(b.getDate());
        });

        return schedule;
    }

    /**
     * Lấy lịch trình trong khoảng thời gian cụ thể
     */
    public List<StudentScheduleDto> getStudentScheduleByDateScope(Integer studentId, String startDateStr, String endDateStr) {
        LocalDate startDate = LocalDate.parse(startDateStr);
        LocalDate endDate = LocalDate.parse(endDateStr);
        
        List<StudentScheduleDto> allSchedule = getStudentSchedule(studentId);
        
        return allSchedule.stream()
            .filter(item -> !item.getDate().isBefore(startDate) && !item.getDate().isAfter(endDate))
            .collect(Collectors.toList());
    }

    /**
     * Lấy lịch trình sắp tới (30 ngày tiếp theo)
     */
    public List<StudentScheduleDto> getUpcomingSchedule(Integer studentId) {
        LocalDate today = LocalDate.now();
        LocalDate nextMonth = today.plusDays(30);
        
        List<StudentScheduleDto> schedule = getStudentSchedule(studentId);
        
        return schedule.stream()
            .filter(item -> !item.getDate().isBefore(today) && !item.getDate().isAfter(nextMonth))
            .collect(Collectors.toList());
    }

    /**
     * Chuyển đổi StudentDefense thành StudentScheduleDto
     */
    private StudentScheduleDto convertToScheduleDto(StudentDefense studentDefense) {
        DefenseSession session = studentDefense.getDefenseSession();
        
        // Xác định trạng thái
        String status = determineStatus(session.getDefenseDate(), session.getStatus().toString());
        
        return StudentScheduleDto.builder()
            .scheduleId(studentDefense.getStudentDefenseId())
            .eventType("defense")
            .title("Bảo vệ đồ án")
            .date(session.getDefenseDate())
            .time(session.getStartTime().toLocalTime())
            .location(session.getLocation())
            .supervisor(getSupervisorName(studentDefense.getSupervisorId()))
            .status(status)
            .description(String.format("Bảo vệ đồ án: %s", studentDefense.getTopicTitle()))
            .sessionId(session.getSessionId())
            .topicId(studentDefense.getTopicId())
            .build();
    }

    /**
     * Chuyển đổi deadline thành StudentScheduleDto
     */
    private StudentScheduleDto convertDeadlineToScheduleDto(Map<String, Object> deadline, Integer studentId) {
        String title = (String) deadline.getOrDefault("title", "Deadline");
        String dateStr = (String) deadline.getOrDefault("dueDate", "");
        String type = (String) deadline.getOrDefault("type", "deadline");
        
        LocalDate date = LocalDate.now();
        try {
            if (!dateStr.isEmpty()) {
                date = LocalDate.parse(dateStr);
            }
        } catch (Exception e) {
            log.warn("Không thể parse deadline date: {}", dateStr);
        }

        String status = determineStatus(date, type.equals("urgent") ? "URGENT" : "SCHEDULED");

        return StudentScheduleDto.builder()
            .scheduleId((Integer) deadline.getOrDefault("id", 0))
            .eventType(type)
            .title(title)
            .date(date)
            .time(type.equals("deadline") ? null : LocalTime.of(23, 59)) // Deadline usually at end of day
            .location("Online")
            .supervisor(getSupervisorNameForStudent(studentId))
            .status(status)
            .description((String) deadline.getOrDefault("description", ""))
            .topicId((Integer) deadline.getOrDefault("topicId", null))
            .build();
    }

    /**
     * Xác định trạng thái dựa trên ngày và trạng thái session
     */
    private String determineStatus(LocalDate date, String sessionStatus) {
        LocalDate today = LocalDate.now();
        long daysUntil = java.time.temporal.ChronoUnit.DAYS.between(today, date);
        
        if (sessionStatus.equals("URGENT") || sessionStatus.equals("COMPLETED")) {
            return sessionStatus.equals("URGENT") ? "urgent" : "completed";
        }
        
        if (daysUntil <= 3) {
            return "urgent";
        } else if (daysUntil <= 7) {
            return "upcoming";
        } else {
            return "upcoming";
        }
    }

    /**
     * Lấy tên giảng viên hướng dẫn
     */
    private String getSupervisorName(Integer supervisorId) {
        if (supervisorId == null) return "Chưa xác định";
        
        try {
            Map<String, Object> supervisor = thesisServiceClient.getSupervisorById(supervisorId);
            return (String) supervisor.getOrDefault("fullName", "Thầy/Cô hướng dẫn");
        } catch (Exception e) {
            log.error("Không thể lấy thông tin giảng viên ID {}: {}", supervisorId, e.getMessage());
            return "Thầy/Cô hướng dẫn";
        }
    }

    /**
     * Lấy tên giảng viên hướng dẫn cho sinh viên
     */
    private String getSupervisorNameForStudent(Integer studentId) {
        try {
            Map<String, Object> studentInfo = thesisServiceClient.getStudentInfo(studentId);
            Integer supervisorId = (Integer) studentInfo.get("supervisorId");
            return getSupervisorName(supervisorId);
        } catch (Exception e) {
            log.error("Không thể lấy thông tin giảng viên hướng dẫn cho sinh viên ID {}: {}", studentId, e.getMessage());
            return "Thầy/Cô hướng dẫn";
        }
    }
}
