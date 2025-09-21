package com.phenikaa.thesisservice.service.implement;

import com.phenikaa.thesisservice.client.NotificationServiceClient;
import com.phenikaa.thesisservice.dto.request.NotificationRequest;
import com.phenikaa.thesisservice.dto.request.SuggestTopicRequest;
import com.phenikaa.thesisservice.dto.response.GetSuggestTopicResponse;
import com.phenikaa.thesisservice.dto.response.GetThesisResponse;
import com.phenikaa.thesisservice.entity.ProjectTopic;
import com.phenikaa.thesisservice.entity.SuggestedTopic;
import com.phenikaa.thesisservice.entity.RegistrationPeriod;
import com.phenikaa.thesisservice.entity.LecturerCapacity;
import com.phenikaa.thesisservice.mapper.SuggestTopicMapper;
import com.phenikaa.thesisservice.repository.ProjectTopicRepository;
import com.phenikaa.thesisservice.repository.SuggestRepository;
import com.phenikaa.thesisservice.repository.RegistrationPeriodRepository;
import com.phenikaa.thesisservice.repository.LecturerCapacityRepository;
import com.phenikaa.thesisservice.repository.RegisterRepository;
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
import java.util.List;
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
    private final RegisterRepository registerRepository;

    @Override
    public void suggestTopic(SuggestTopicRequest dto, Integer studentId) {
        if (dto.getRegistrationPeriodId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Thiếu registrationPeriodId");
        }
        RegistrationPeriod activePeriod = registrationPeriodRepository.findById(dto.getRegistrationPeriodId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Đợt đăng ký không tồn tại"));
        // Xác thực đợt còn hiệu lực và đang ACTIVE
        LocalDateTime now = LocalDateTime.now();
        boolean activeWindow = activePeriod.getStatus() == RegistrationPeriod.PeriodStatus.ACTIVE
                && !now.isBefore(activePeriod.getStartDate())
                && !now.isAfter(activePeriod.getEndDate());
        if (!activeWindow) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Đợt đăng ký hiện không mở");
        }

        // Kiểm tra xem sinh viên đã đề xuất đề tài trong đợt này chưa
        if (hasStudentSuggestedInPeriod(studentId, activePeriod.getPeriodId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Bạn đã đề xuất đề tài trong đợt này rồi!");
        }

        // Kiểm tra xem sinh viên đã đăng ký đề tài trong đợt này chưa
        if (hasStudentRegisteredInPeriod(studentId, activePeriod.getPeriodId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Bạn đã đăng ký đề tài trong đợt này rồi! Không thể đề xuất đề tài khác.");
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

        // Cập nhật sức chứa của giảng viên - giảm maxStudents
        System.out.println("=== TRƯỚC KHI GIẢM maxStudents ===");
        System.out.println("Lecturer ID: " + dto.getSupervisorId());
        System.out.println("Period ID: " + activePeriod.getPeriodId());

        updateLecturerCapacity(dto.getSupervisorId(), activePeriod.getPeriodId(), true);

        // Kiểm tra sau khi giảm
        LecturerCapacity afterCapacity = lecturerCapacityRepository
                .findByLecturerIdAndRegistrationPeriodId(dto.getSupervisorId(), activePeriod.getPeriodId())
                .orElse(null);
        if (afterCapacity != null) {
            System.out.println("Capacity sau khi giảm: maxStudents=" + afterCapacity.getMaxStudents() + ", currentStudents=" + afterCapacity.getCurrentStudents());
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
                    .maxStudents(period.getMaxStudentsPerLecturer())
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
                    RegistrationPeriod period = registrationPeriodRepository.findById(periodId)
                            .orElseThrow(() -> new RuntimeException("Period not found with id: " + periodId));

                    return LecturerCapacity.builder()
                            .lecturerId(lecturerId)
                            .registrationPeriodId(periodId)
                            .maxStudents(period.getMaxStudentsPerLecturer())
                            .currentStudents(0)
                            .build();
                });

        if (decrease) {
            // Giảm maxStudents khi sinh viên đăng ký/đề xuất thành công
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

    @Override
    public Page<GetSuggestTopicResponse> getSuggestTopicByStudentId(Integer studentId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return suggestRepository.findBySuggestedBy(studentId, pageable)
                .map(suggestTopicMapper::toGetSuggestTopicResponse);
    }

    @Override
    public void updateSuggestTopic(Integer suggestedId, SuggestTopicRequest dto, Integer studentId) {
        // Tìm đề xuất đề tài
        SuggestedTopic suggestedTopic = suggestRepository.findById(suggestedId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Đề xuất đề tài không tồn tại"));

        // Kiểm tra quyền sở hữu
        if (!suggestedTopic.getSuggestedBy().equals(studentId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bạn không có quyền chỉnh sửa đề xuất này");
        }

        // Kiểm tra trạng thái - chỉ cho phép chỉnh sửa khi đang chờ duyệt
        if (suggestedTopic.getSuggestionStatus() != SuggestedTopic.SuggestionStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Chỉ có thể chỉnh sửa đề tài đang chờ duyệt");
        }

        // Kiểm tra đợt đăng ký còn hiệu lực không
        RegistrationPeriod activePeriod = registrationPeriodRepository.findById(suggestedTopic.getRegistrationPeriodId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Đợt đăng ký không tồn tại"));
        
        LocalDateTime now = LocalDateTime.now();
        boolean activeWindow = activePeriod.getStatus() == RegistrationPeriod.PeriodStatus.ACTIVE
                && !now.isBefore(activePeriod.getStartDate())
                && !now.isAfter(activePeriod.getEndDate());
        if (!activeWindow) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Đợt đăng ký hiện không mở, không thể chỉnh sửa");
        }

        // Cập nhật thông tin đề tài
        ProjectTopic topic = suggestedTopic.getProjectTopic();
        topic.setTitle(dto.getTitle());
        topic.setDescription(dto.getDescription());
        topic.setObjectives(dto.getObjectives());
        topic.setMethodology(dto.getMethodology());
        topic.setExpectedOutcome(dto.getExpectedOutcome());
        topic.setSupervisorId(dto.getSupervisorId());
        projectTopicRepository.save(topic);

        // Cập nhật thông tin đề xuất
        suggestedTopic.setReason(dto.getReason());
        suggestedTopic.setSuggestedFor(dto.getSupervisorId());
        suggestRepository.save(suggestedTopic);
    }
}
