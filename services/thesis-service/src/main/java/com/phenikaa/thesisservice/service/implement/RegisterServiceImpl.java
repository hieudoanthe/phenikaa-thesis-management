package com.phenikaa.thesisservice.service.implement;

import com.phenikaa.thesisservice.dto.request.RegisterTopicRequest;
import com.phenikaa.thesisservice.entity.ProjectTopic;
import com.phenikaa.thesisservice.entity.Register;
import com.phenikaa.thesisservice.entity.RegistrationPeriod;
import com.phenikaa.thesisservice.entity.LecturerCapacity;
import com.phenikaa.thesisservice.mapper.RegisterMapper;
import com.phenikaa.thesisservice.repository.ProjectTopicRepository;
import com.phenikaa.thesisservice.repository.RegisterRepository;
import com.phenikaa.thesisservice.repository.RegistrationPeriodRepository;
import com.phenikaa.thesisservice.repository.LecturerCapacityRepository;
import com.phenikaa.thesisservice.repository.SuggestRepository;
import com.phenikaa.thesisservice.service.interfaces.RegisterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class RegisterServiceImpl implements RegisterService {

    private final RegisterMapper registerMapper;
    private final RegisterRepository registerRepository;
    private final ProjectTopicRepository projectTopicRepository;
    private final RegistrationPeriodRepository registrationPeriodRepository;
    private final LecturerCapacityRepository lecturerCapacityRepository;
    private final SuggestRepository suggestRepository;

    @Override
    public void registerTopic(RegisterTopicRequest dto, Integer userId) {
        // Bắt buộc chỉ định registrationPeriodId khi có nhiều đợt song song
        if (dto.getRegistrationPeriodId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Thiếu registrationPeriodId");
        }
        RegistrationPeriod period = registrationPeriodRepository.findById(dto.getRegistrationPeriodId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Đợt đăng ký không tồn tại"));
        // Xác thực period đang ACTIVE và trong khoảng thời gian
        LocalDateTime now = LocalDateTime.now();
        boolean activeWindow = period.getStatus() == RegistrationPeriod.PeriodStatus.ACTIVE
                && !now.isBefore(period.getStartDate())
                && !now.isAfter(period.getEndDate());
        if (!activeWindow) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Đợt đăng ký hiện không mở");
        }

        // Kiểm tra xem sinh viên đã đăng ký trong đợt này chưa
        if (hasStudentRegisteredInPeriod(userId, period.getPeriodId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "Bạn đã đăng ký đề tài trong đợt này rồi!");
        }

        // Kiểm tra xem sinh viên đã đề xuất đề tài trong đợt này chưa
        if (hasStudentSuggestedInPeriod(userId, period.getPeriodId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "Bạn đã đề xuất đề tài trong đợt này rồi! Không thể đăng ký đề tài khác.");
        }

        ProjectTopic topic = projectTopicRepository.findById(dto.getTopicId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Topic not found!"));

        // Kiểm tra sức chứa của giảng viên trong đợt đăng ký này
        if (!canLecturerAcceptMoreStudents(topic.getSupervisorId(), period.getPeriodId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "Giảng viên đã đạt giới hạn số lượng sinh viên trong đợt đăng ký này!");
        }

        Register register = registerMapper.toRegister(dto);
        register.setProjectTopic(topic);
        register.setStudentId(userId);
        register.setRegistrationPeriodId(period.getPeriodId());

        // Lưu đăng ký
        registerRepository.save(register);

        // Cập nhật trạng thái đề tài từ AVAILABLE thành PENDING
        topic.setApprovalStatus(ProjectTopic.ApprovalStatus.PENDING);
        projectTopicRepository.save(topic);

        // Cập nhật sức chứa của giảng viên
        updateLecturerCapacity(topic.getSupervisorId(), period.getPeriodId(), true);
    }

    private boolean hasStudentRegisteredInPeriod(Integer studentId, Integer periodId) {
        return registerRepository.existsByStudentIdAndRegistrationPeriodId(studentId, periodId);
    }

    private boolean hasStudentSuggestedInPeriod(Integer studentId, Integer periodId) {
        return suggestRepository.existsBySuggestedByAndRegistrationPeriodId(studentId, periodId);
    }

    private boolean canLecturerAcceptMoreStudents(Integer lecturerId, Integer periodId) {
        // Tìm capacity hiện tại
        LecturerCapacity capacity = lecturerCapacityRepository
                .findByLecturerIdAndRegistrationPeriodId(lecturerId, periodId)
                .orElse(null);
        
        // Nếu không có capacity, tạo mới với giá trị từ period
        if (capacity == null) {
            // Lấy thông tin period để biết maxStudentsPerLecturer
            RegistrationPeriod period = registrationPeriodRepository.findById(periodId)
                    .orElseThrow(() -> new RuntimeException("Period not found with id: " + periodId));
            
            capacity = LecturerCapacity.builder()
                    .lecturerId(lecturerId)
                    .registrationPeriodId(periodId)
                    .maxStudents(period.getMaxStudentsPerLecturer()) // Lấy từ period
                    .currentStudents(0)
                    .build();
            
            // Lưu capacity mới
            lecturerCapacityRepository.save(capacity);
            System.out.println("Đã tạo LecturerCapacity mới cho lecturer " + lecturerId + " trong period " + periodId + " với maxStudents=" + period.getMaxStudentsPerLecturer()); // Debug log
        }
        
        System.out.println("Kiểm tra capacity: " + capacity); // Debug log
        // Kiểm tra xem còn slot trống không (maxStudents > 0)
        boolean canAccept = capacity.getMaxStudents() > 0;
        System.out.println("Có thể nhận thêm sinh viên: " + canAccept + " (maxStudents=" + capacity.getMaxStudents() + ")"); // Debug log
        
        return canAccept;
    }

    private void updateLecturerCapacity(Integer lecturerId, Integer periodId, boolean decrease) {
        LecturerCapacity capacity = lecturerCapacityRepository
                .findByLecturerIdAndRegistrationPeriodId(lecturerId, periodId)
                .orElseGet(() -> {
                    // Lấy thông tin period để biết maxStudentsPerLecturer
                    RegistrationPeriod period = registrationPeriodRepository.findById(periodId)
                            .orElseThrow(() -> new RuntimeException("Period not found with id: " + periodId));
                    
                    return LecturerCapacity.builder()
                            .lecturerId(lecturerId)
                            .registrationPeriodId(periodId)
                            .maxStudents(period.getMaxStudentsPerLecturer()) // Lấy từ period
                            .currentStudents(0)
                            .build();
                });

        if (decrease) {
            // Giảm maxStudents khi sinh viên đăng ký thành công
            if (capacity.getMaxStudents() > 0) {
                capacity.setMaxStudents(capacity.getMaxStudents() - 1);
                System.out.println("Đã giảm maxStudents cho lecturer " + lecturerId + " trong period " + periodId + ". Còn lại: " + capacity.getMaxStudents());
            }
        } else {
            // Tăng maxStudents khi giảng viên từ chối (hoàn trả slot)
            capacity.setMaxStudents(capacity.getMaxStudents() + 1);
            System.out.println("Đã tăng maxStudents cho lecturer " + lecturerId + " trong period " + periodId + ". Hiện tại: " + capacity.getMaxStudents());
        }

        lecturerCapacityRepository.save(capacity);
    }

    // Statistics methods implementation
    @Override
    public Long getRegistrationCount() {
        return registerRepository.count();
    }

    @Override
    public Long getRegistrationCountByStatus(String status) {
        try {
            Register.RegisterStatus registerStatus = Register.RegisterStatus.valueOf(status.toUpperCase());
            return registerRepository.countByRegisterStatus(registerStatus);
        } catch (IllegalArgumentException e) {
            return 0L;
        }
    }

    @Override
    public Long getRegistrationCountByAcademicYear(Integer academicYearId) {
        return registerRepository.countByRegistrationPeriodId(academicYearId);
    }

    @Override
    public List<Map<String, Object>> getRegistrationsByTopic(Integer topicId) {
        List<Register> registrations = registerRepository.findByProjectTopicTopicId(topicId);
        return registrations.stream()
                .map(register -> {
                    Map<String, Object> registrationMap = new HashMap<>();
                    registrationMap.put("registerId", register.getRegisterId());
                    registrationMap.put("studentId", register.getStudentId());
                    registrationMap.put("registerType", register.getRegisterType());
                    registrationMap.put("registerStatus", register.getRegisterStatus());
                    registrationMap.put("registeredAt", register.getRegisteredAt());
                    registrationMap.put("approvedAt", register.getApprovedAt());
                    registrationMap.put("approvedBy", register.getApprovedBy());
                    return registrationMap;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getRegistrationsOverTime(String startDate, String endDate) {
        // TODO: Implement registrations over time with date filtering
        List<Register> registrations = registerRepository.findAll();
        return registrations.stream()
                .collect(Collectors.groupingBy(
                        registration -> registration.getRegisteredAt().atZone(java.time.ZoneId.systemDefault()).toLocalDate().toString(),
                        Collectors.counting()
                ))
                .entrySet().stream()
                .map(entry -> {
                    Map<String, Object> timeData = new HashMap<>();
                    timeData.put("date", entry.getKey());
                    timeData.put("count", entry.getValue());
                    return timeData;
                })
                .sorted((a, b) -> a.get("date").toString().compareTo(b.get("date").toString()))
                .collect(Collectors.toList());
    }
    
    @Override
    public Long getRegistrationsToday() {
        log.info("Getting registrations count for today");
        LocalDate today = LocalDate.now();
        return registerRepository.countByRegisteredAtBetween(
            today.atStartOfDay().atZone(java.time.ZoneId.systemDefault()).toInstant(),
            today.plusDays(1).atStartOfDay().atZone(java.time.ZoneId.systemDefault()).toInstant()
        );
    }
    
    @Override
    public List<Map<String, Object>> getTodayRegistrations() {
        log.info("Getting today's registrations");
        LocalDate today = LocalDate.now();
        List<Register> todayRegistrations = registerRepository.findByRegisteredAtBetween(
            today.atStartOfDay().atZone(java.time.ZoneId.systemDefault()).toInstant(),
            today.plusDays(1).atStartOfDay().atZone(java.time.ZoneId.systemDefault()).toInstant()
        );
        
        return todayRegistrations.stream()
                .map(registration -> {
                    Map<String, Object> registrationData = new HashMap<>();
                    registrationData.put("id", registration.getRegisterId());
                    registrationData.put("studentId", registration.getStudentId());
                    registrationData.put("topicId", registration.getProjectTopic().getTopicId());
                    registrationData.put("topicTitle", registration.getProjectTopic().getTitle());
                    registrationData.put("supervisorId", registration.getProjectTopic().getSupervisorId());
                    registrationData.put("status", registration.getRegisterStatus());
                    registrationData.put("registeredAt", registration.getRegisteredAt());
                    return registrationData;
                })
                .collect(Collectors.toList());
    }
}
