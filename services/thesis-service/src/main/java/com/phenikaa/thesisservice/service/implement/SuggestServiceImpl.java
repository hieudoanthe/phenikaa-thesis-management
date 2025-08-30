package com.phenikaa.thesisservice.service.implement;

import com.phenikaa.thesisservice.client.NotificationServiceClient;
import com.phenikaa.thesisservice.dto.request.NotificationRequest;
import com.phenikaa.thesisservice.dto.request.SuggestTopicRequest;
import com.phenikaa.thesisservice.dto.response.GetSuggestTopicResponse;
import com.phenikaa.thesisservice.entity.ProjectTopic;
import com.phenikaa.thesisservice.entity.SuggestedTopic;
import com.phenikaa.thesisservice.entity.RegistrationPeriod;
import com.phenikaa.thesisservice.entity.LecturerCapacity;
import com.phenikaa.thesisservice.mapper.SuggestTopicMapper;
import com.phenikaa.thesisservice.repository.ProjectTopicRepository;
import com.phenikaa.thesisservice.repository.SuggestRepository;
import com.phenikaa.thesisservice.repository.RegistrationPeriodRepository;
import com.phenikaa.thesisservice.repository.LecturerCapacityRepository;
import com.phenikaa.thesisservice.service.interfaces.SuggestService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SuggestServiceImpl implements SuggestService {
    private final SuggestTopicMapper suggestTopicMapper;
    private final ProjectTopicRepository projectTopicRepository;
    private final SuggestRepository suggestRepository;
    private final NotificationServiceClient notificationServiceClient;
    private final RegistrationPeriodRepository registrationPeriodRepository;
    private final LecturerCapacityRepository lecturerCapacityRepository;

    @Override
    public void suggestTopic(SuggestTopicRequest dto, Integer studentId) {
        // Kiểm tra xem có đợt đăng ký nào đang hoạt động không
        RegistrationPeriod activePeriod = registrationPeriodRepository.findActivePeriod(LocalDateTime.now())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "Hiện tại không có đợt đăng ký nào đang diễn ra!"));

        // Kiểm tra xem sinh viên đã đề xuất đề tài trong đợt này chưa
        if (hasStudentSuggestedInPeriod(studentId, activePeriod.getPeriodId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "Bạn đã đề xuất đề tài trong đợt này rồi!");
        }

        // Kiểm tra sức chứa của giảng viên trong đợt đăng ký này
        if (!canLecturerAcceptMoreStudents(dto.getSupervisorId(), activePeriod.getPeriodId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "Giảng viên đã đạt giới hạn số lượng sinh viên trong đợt đăng ký này!");
        }

        ProjectTopic topic = suggestTopicMapper.toProjectTopic(dto);
        topic.setCreatedBy(studentId);
        topic.setTopicCode(UUID.randomUUID().toString());
        topic.setTopicStatus(ProjectTopic.TopicStatus.ACTIVE);
        projectTopicRepository.save(topic);

        SuggestedTopic suggested = SuggestedTopic.builder()
                .projectTopic(topic)
                .suggestedBy(studentId)
                .suggestedFor(dto.getSupervisorId())
                .reason(dto.getReason())
                .suggestionStatus(SuggestedTopic.SuggestionStatus.PENDING)
                .registrationPeriodId(activePeriod.getPeriodId())
                .build();
        suggestRepository.save(suggested);

        // Cập nhật sức chứa của giảng viên
        System.out.println("=== TRƯỚC KHI TĂNG currentStudents ===");
        System.out.println("Lecturer ID: " + dto.getSupervisorId());
        System.out.println("Period ID: " + activePeriod.getPeriodId());
        
        updateLecturerCapacity(dto.getSupervisorId(), activePeriod.getPeriodId(), true);
        
        // Kiểm tra sau khi tăng
        LecturerCapacity afterCapacity = lecturerCapacityRepository
                .findByLecturerIdAndRegistrationPeriodId(dto.getSupervisorId(), activePeriod.getPeriodId())
                .orElse(null);
        if (afterCapacity != null) {
            System.out.println("Capacity sau khi tăng: maxStudents=" + afterCapacity.getMaxStudents() + ", currentStudents=" + afterCapacity.getCurrentStudents());
        }

        NotificationRequest noti = new NotificationRequest(
                studentId,
                dto.getSupervisorId(),
                "Bạn có một đề tài mới cần duyệt!"
        );
        notificationServiceClient.sendNotification(noti);
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
            System.out.println("Tăng currentStudents từ " + capacity.getCurrentStudents() + " lên " + (capacity.getCurrentStudents() + 1));
            System.out.println("Giảm maxStudents từ " + capacity.getMaxStudents() + " xuống " + (capacity.getMaxStudents() - 1));
            capacity.increaseCurrentStudents();
            capacity.setMaxStudents(capacity.getMaxStudents() - 1); // Giảm maxStudents khi tăng currentStudents
        } else {
            System.out.println("Giảm currentStudents từ " + capacity.getCurrentStudents() + " xuống " + (capacity.getCurrentStudents() - 1));
            capacity.decreaseCurrentStudents();
            // KHÔNG thay đổi maxStudents khi giảm currentStudents
        }

        lecturerCapacityRepository.save(capacity);
        System.out.println("Đã lưu capacity: maxStudents=" + capacity.getMaxStudents() + ", currentStudents=" + capacity.getCurrentStudents());
    }

    @Override
    public Page<GetSuggestTopicResponse> getSuggestTopicByStudentId(Integer studentId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return suggestRepository.findBySuggestedBy(studentId, pageable)
                .map(suggestTopicMapper::toGetSuggestTopicResponse);
    }
}
