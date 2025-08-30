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
import com.phenikaa.thesisservice.service.interfaces.RegisterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RegisterServiceImpl implements RegisterService {

    private final RegisterMapper registerMapper;
    private final RegisterRepository registerRepository;
    private final ProjectTopicRepository projectTopicRepository;
    private final RegistrationPeriodRepository registrationPeriodRepository;
    private final LecturerCapacityRepository lecturerCapacityRepository;

    @Override
    public void registerTopic(RegisterTopicRequest dto, Integer userId) {
        // Kiểm tra xem có đợt đăng ký nào đang hoạt động không
        RegistrationPeriod activePeriod = registrationPeriodRepository.findActivePeriod(LocalDateTime.now())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "Hiện tại không có đợt đăng ký nào đang diễn ra!"));

        // Kiểm tra xem sinh viên đã đăng ký trong đợt này chưa
        if (hasStudentRegisteredInPeriod(userId, activePeriod.getPeriodId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "Bạn đã đăng ký đề tài trong đợt này rồi!");
        }

        ProjectTopic topic = projectTopicRepository.findById(dto.getTopicId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Topic not found!"));

        // Kiểm tra sức chứa của giảng viên trong đợt đăng ký này
        if (!canLecturerAcceptMoreStudents(topic.getSupervisorId(), activePeriod.getPeriodId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "Giảng viên đã đạt giới hạn số lượng sinh viên trong đợt đăng ký này!");
        }

        Register register = registerMapper.toRegister(dto);
        register.setProjectTopic(topic);
        register.setStudentId(userId);
        register.setRegistrationPeriodId(activePeriod.getPeriodId());

        // Lưu đăng ký
        registerRepository.save(register);

        // Cập nhật sức chứa của giảng viên
        updateLecturerCapacity(topic.getSupervisorId(), activePeriod.getPeriodId(), true);
    }

    private boolean hasStudentRegisteredInPeriod(Integer studentId, Integer periodId) {
        return registerRepository.existsByStudentIdAndRegistrationPeriodId(studentId, periodId);
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
        System.out.println("Có thể nhận thêm sinh viên: " + capacity.canAcceptMoreStudents()); // Debug log
        
        return capacity.canAcceptMoreStudents();
    }

    private void updateLecturerCapacity(Integer lecturerId, Integer periodId, boolean increase) {
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

        if (increase) {
            capacity.increaseCurrentStudents();
            System.out.println("Tăng số sinh viên cho lecturer " + lecturerId + " trong period " + periodId + ". Hiện tại: " + capacity.getCurrentStudents()); // Debug log
        } else {
            capacity.decreaseCurrentStudents();
            System.out.println("Giảm số sinh viên cho lecturer " + lecturerId + " trong period " + periodId + ". Hiện tại: " + capacity.getCurrentStudents()); // Debug log
        }

        lecturerCapacityRepository.save(capacity);
    }
}
