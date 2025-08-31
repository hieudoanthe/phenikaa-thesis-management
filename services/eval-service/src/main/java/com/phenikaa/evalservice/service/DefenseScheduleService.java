package com.phenikaa.evalservice.service;

import com.phenikaa.evalservice.dto.DefenseScheduleDto;
import com.phenikaa.evalservice.entity.DefenseSchedule;
import com.phenikaa.evalservice.repository.DefenseScheduleRepository;
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
public class DefenseScheduleService {

    private final DefenseScheduleRepository defenseScheduleRepository;

    /**
     * Tạo lịch bảo vệ mới
     */
    public DefenseScheduleDto createSchedule(DefenseScheduleDto scheduleDto) {
        log.info("Tạo lịch bảo vệ mới: {}", scheduleDto.getScheduleName());
        
        DefenseSchedule schedule = DefenseSchedule.builder()
                .scheduleName(scheduleDto.getScheduleName())
                .academicYearId(scheduleDto.getAcademicYearId())
                .startDate(scheduleDto.getStartDate())
                .endDate(scheduleDto.getEndDate())
                .location(scheduleDto.getLocation())
                .description(scheduleDto.getDescription())
                .status(DefenseSchedule.ScheduleStatus.PLANNING)
                .createdBy(scheduleDto.getCreatedBy())
                .build();

        DefenseSchedule savedSchedule = defenseScheduleRepository.save(schedule);
        log.info("Đã tạo lịch bảo vệ với ID: {}", savedSchedule.getScheduleId());
        
        return DefenseScheduleDto.fromEntity(savedSchedule);
    }

    /**
     * Cập nhật lịch bảo vệ
     */
    public DefenseScheduleDto updateSchedule(Integer scheduleId, DefenseScheduleDto scheduleDto) {
        log.info("Cập nhật lịch bảo vệ ID: {}", scheduleId);
        
        DefenseSchedule existingSchedule = defenseScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lịch bảo vệ với ID: " + scheduleId));

        existingSchedule.setScheduleName(scheduleDto.getScheduleName());
        existingSchedule.setAcademicYearId(scheduleDto.getAcademicYearId());
        existingSchedule.setStartDate(scheduleDto.getStartDate());
        existingSchedule.setEndDate(scheduleDto.getEndDate());
        existingSchedule.setLocation(scheduleDto.getLocation());
        existingSchedule.setDescription(scheduleDto.getDescription());

        DefenseSchedule updatedSchedule = defenseScheduleRepository.save(existingSchedule);
        log.info("Đã cập nhật lịch bảo vệ ID: {}", scheduleId);
        
        return DefenseScheduleDto.fromEntity(updatedSchedule);
    }

    /**
     * Lấy lịch bảo vệ theo ID
     */
    public DefenseScheduleDto getScheduleById(Integer scheduleId) {
        DefenseSchedule schedule = defenseScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lịch bảo vệ với ID: " + scheduleId));
        
        return DefenseScheduleDto.fromEntity(schedule);
    }

    /**
     * Lấy tất cả lịch bảo vệ
     */
    public List<DefenseScheduleDto> getAllSchedules() {
        List<DefenseSchedule> schedules = defenseScheduleRepository.findAll();
        return schedules.stream()
                .map(DefenseScheduleDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Lấy lịch bảo vệ theo năm học
     */
    public List<DefenseScheduleDto> getSchedulesByAcademicYear(Integer academicYearId) {
        List<DefenseSchedule> schedules = defenseScheduleRepository.findByAcademicYearId(academicYearId);
        return schedules.stream()
                .map(DefenseScheduleDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Lấy lịch bảo vệ theo trạng thái
     */
    public List<DefenseScheduleDto> getSchedulesByStatus(DefenseSchedule.ScheduleStatus status) {
        List<DefenseSchedule> schedules = defenseScheduleRepository.findByStatus(status);
        return schedules.stream()
                .map(DefenseScheduleDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Lấy lịch bảo vệ đang hoạt động
     */
    public DefenseScheduleDto getActiveSchedule() {
        DefenseSchedule activeSchedule = defenseScheduleRepository.findActiveSchedule(LocalDate.now())
                .orElse(null);
        
        return activeSchedule != null ? DefenseScheduleDto.fromEntity(activeSchedule) : null;
    }

    /**
     * Kích hoạt lịch bảo vệ
     */
    public void activateSchedule(Integer scheduleId) {
        log.info("Kích hoạt lịch bảo vệ ID: {}", scheduleId);
        
        DefenseSchedule schedule = defenseScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lịch bảo vệ với ID: " + scheduleId));

        // Hủy kích hoạt các lịch khác
        List<DefenseSchedule> otherActiveSchedules = defenseScheduleRepository.findByStatus(DefenseSchedule.ScheduleStatus.ACTIVE);
        for (DefenseSchedule otherSchedule : otherActiveSchedules) {
            if (!otherSchedule.getScheduleId().equals(scheduleId)) {
                otherSchedule.setStatus(DefenseSchedule.ScheduleStatus.COMPLETED);
                defenseScheduleRepository.save(otherSchedule);
            }
        }

        // Kích hoạt lịch hiện tại
        schedule.setStatus(DefenseSchedule.ScheduleStatus.ACTIVE);
        defenseScheduleRepository.save(schedule);
        
        log.info("Đã kích hoạt lịch bảo vệ ID: {}", scheduleId);
    }

    /**
     * Hủy kích hoạt lịch bảo vệ
     */
    public void deactivateSchedule(Integer scheduleId) {
        log.info("Hủy kích hoạt lịch bảo vệ ID: {}", scheduleId);
        
        DefenseSchedule schedule = defenseScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lịch bảo vệ với ID: " + scheduleId));

        schedule.setStatus(DefenseSchedule.ScheduleStatus.COMPLETED);
        defenseScheduleRepository.save(schedule);
        
        log.info("Đã hủy kích hoạt lịch bảo vệ ID: {}", scheduleId);
    }

    /**
     * Xóa lịch bảo vệ
     */
    public void deleteSchedule(Integer scheduleId) {
        log.info("Xóa lịch bảo vệ ID: {}", scheduleId);
        
        DefenseSchedule schedule = defenseScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lịch bảo vệ với ID: " + scheduleId));

        // Kiểm tra xem có thể xóa không
        if (schedule.getStatus() == DefenseSchedule.ScheduleStatus.ACTIVE) {
            throw new RuntimeException("Không thể xóa lịch bảo vệ đang hoạt động");
        }

        defenseScheduleRepository.delete(schedule);
        log.info("Đã xóa lịch bảo vệ ID: {}", scheduleId);
    }

    /**
     * Lấy lịch bảo vệ theo khoảng thời gian
     */
    public List<DefenseScheduleDto> getSchedulesByDateRange(LocalDate startDate, LocalDate endDate) {
        List<DefenseSchedule> schedules = defenseScheduleRepository.findByDateRange(startDate, endDate);
        return schedules.stream()
                .map(DefenseScheduleDto::fromEntity)
                .collect(Collectors.toList());
    }
}
