package com.phenikaa.thesisservice.service.implement;

import com.phenikaa.thesisservice.client.NotificationServiceClient;
import com.phenikaa.thesisservice.client.ProfileServiceClient;
import com.phenikaa.thesisservice.dto.request.CreateProjectTopicRequest;
import com.phenikaa.thesisservice.dto.request.EditProjectTopicRequest;
import com.phenikaa.thesisservice.dto.request.NotificationRequest;
import com.phenikaa.thesisservice.dto.request.UpdateProjectTopicRequest;
import com.phenikaa.thesisservice.dto.request.ThesisFilterRequest;
import com.phenikaa.thesisservice.dto.request.ThesisQbeRequest;
import com.phenikaa.thesisservice.dto.response.AvailableTopicResponse;
import com.phenikaa.thesisservice.dto.response.GetThesisResponse;
import com.phenikaa.thesisservice.entity.Register;
import com.phenikaa.thesisservice.entity.SuggestedTopic;
import com.phenikaa.thesisservice.mapper.ProjectTopicMapper;
import com.phenikaa.thesisservice.repository.ProjectTopicRepository;
import com.phenikaa.thesisservice.entity.ProjectTopic;
import com.phenikaa.thesisservice.projection.ProjectTopicSummary;
import com.phenikaa.thesisservice.dto.response.ProjectTopicSummaryDto;
import com.phenikaa.thesisservice.service.interfaces.ThesisService;
import com.phenikaa.thesisservice.specification.ThesisSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.HashMap;

@Service
@RequiredArgsConstructor
public class ThesisServiceImpl implements ThesisService {

    private final ProjectTopicRepository projectTopicRepository;

    private final ProjectTopicMapper projectTopicMapper;

    private final NotificationServiceClient notificationServiceClient;
    private final ProfileServiceClient profileServiceClient;

    @Override
    public ProjectTopic createProjectTopic(CreateProjectTopicRequest dto, Integer userId) {
        ProjectTopic entity = ProjectTopic.builder()
                .topicCode(dto.getTopicCode())
                .title(dto.getTitle())
                .description(dto.getDescription())
                .objectives(dto.getObjectives())
                .methodology(dto.getMethodology())
                .expectedOutcome(dto.getExpectedOutcome())
                .maxStudents(dto.getMaxStudents())
                .academicYearId(dto.getAcademicYearId())
                .difficultyLevel(dto.getDifficultyLevel())
                .createdBy(userId)
                .supervisorId(userId)
                .updatedBy(userId)
                .build();
        return projectTopicRepository.save(entity);
    }

    @Override
    public List<GetThesisResponse> findAll() {
        return projectTopicRepository.findAllWithAssociations()
                .stream()
                .map(e -> GetThesisResponse.builder()
                        .topicId(e.getTopicId())
                        .topicCode(e.getTopicCode())
                        .title(e.getTitle())
                        .description(e.getDescription())
                        .objectives(e.getObjectives())
                        .methodology(e.getMethodology())
                        .expectedOutcome(e.getExpectedOutcome())
                        .academicYearId(e.getAcademicYearId())
                        .maxStudents(e.getMaxStudents())
                        .difficultyLevel(e.getDifficultyLevel())
                        .topicStatus(e.getTopicStatus())
                        .approvalStatus(e.getApprovalStatus())
                        .build()
                ).collect(Collectors.toList());
    }

    @Override
    public Page<GetThesisResponse> getTopicsByTeacherId(Integer teacherId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("topicId").ascending());
        return projectTopicRepository.findBySupervisorId(teacherId, pageable)
                .map(projectTopic -> {
                    GetThesisResponse dto = projectTopicMapper.toResponse(projectTopic);

                    dto.setSuggestedBy(
                            projectTopic.getSuggestedTopics().stream()
                                    .findFirst()
                                    .map(SuggestedTopic::getSuggestedBy)
                                    .orElse(null)
                    );

                    dto.setRegisterId(
                            projectTopic.getRegisters().stream()
                                    .findFirst()
                                    .map(Register::getRegisterId)
                                    .orElse(null)
                    );

                    return dto;
                });
    }

    @Override
    public ProjectTopic editProjectTopic(EditProjectTopicRequest dto) {
        ProjectTopic entity = projectTopicRepository.findById(dto.getTopicId()).orElseThrow(() -> new RuntimeException("Not found"));
        projectTopicMapper.editProjectTopic(dto, entity);
        return projectTopicRepository.save(entity);
    }

    @Override
    public ProjectTopic updateProjectTopic(UpdateProjectTopicRequest dto) {
        ProjectTopic entity = projectTopicRepository.findById(dto.getTopicId()).orElseThrow(() -> new RuntimeException("Not found"));
        projectTopicMapper.updateProjectTopic(dto, entity);
        return projectTopicRepository.save(entity);
    }

    @Override
    public void deleteTopic(Integer topicId) {
        ProjectTopic entity = projectTopicRepository.findById(topicId).orElseThrow(() -> new RuntimeException("Not found"));
        projectTopicRepository.delete(entity);
    }

    @Override
    public List<AvailableTopicResponse> getAvailableTopics() {
        List<ProjectTopic> topics = projectTopicRepository.findByApprovalStatusAndTopicStatus(
                ProjectTopic.ApprovalStatus.AVAILABLE,
                ProjectTopic.TopicStatus.ACTIVE
        );

        return topics.stream()
                .map(projectTopicMapper::toAvailableTopicDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void approvedTopic(Integer topicId) {
        ProjectTopic projectTopic = projectTopicRepository.findById(topicId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project topic not found!"));

        // Kiểm tra xem đề tài có thể được approve không
        if (!projectTopic.canBeApproved()) {
            if (projectTopic.isFull()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "Đề tài đã đủ số lượng sinh viên, không thể approve thêm!");
            } else if (projectTopic.getApprovalStatus() == ProjectTopic.ApprovalStatus.APPROVED) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "Đề tài đã được approve trước đó!");
            } else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "Đề tài không thể được approve!");
            }
        }

        Integer senderId = projectTopic.getSupervisorId();

        SuggestedTopic suggestedTopic = projectTopic.getSuggestedTopics().stream()
                .filter(st -> st.getSuggestionStatus() == SuggestedTopic.SuggestionStatus.PENDING)
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "No pending suggestion found!"));

        Integer receiverId = suggestedTopic.getSuggestedBy();

        // Giảm số lượng sinh viên có thể nhận
        boolean decreased = projectTopic.decreaseMaxStudents();
        if (!decreased) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "Không thể giảm số lượng sinh viên, đề tài đã đủ!");
        }

        // Cập nhật trạng thái đề tài
        projectTopic.updateTopicStatusBasedOnStudents();
        
        // Cập nhật trạng thái suggestion
        suggestedTopic.setSuggestionStatus(SuggestedTopic.SuggestionStatus.APPROVED);
        suggestedTopic.setApprovedBy(senderId);

        // Giảm chỉ tiêu giảng viên trên profile-service trước khi lưu
        profileServiceClient.decreaseTeacherCapacity();

        // Lưu thay đổi vào database
        projectTopicRepository.save(projectTopic);

        // Gửi thông báo
        String message;
        if (projectTopic.isFull()) {
            message = "Đề tài '" + projectTopic.getTitle() + "' đã được duyệt và đã đủ số lượng sinh viên!";
        } else {
            message = "Đề tài '" + projectTopic.getTitle() + "' đã được duyệt! Còn " + 
                     projectTopic.getRemainingStudentSlots() + " chỗ trống.";
        }

        NotificationRequest notification = new NotificationRequest(
                senderId,
                receiverId,
                message
        );
        notificationServiceClient.sendNotification(notification);
    }

    @Override
    public void rejectTopic(Integer topicId) {
        Optional<ProjectTopic> projectTopicOpt = projectTopicRepository.findById(topicId);
        if (projectTopicOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Project topic not found!");
        }
        ProjectTopic projectTopic = projectTopicOpt.get();
        projectTopic.setApprovalStatus(ProjectTopic.ApprovalStatus.REJECTED);
        projectTopicRepository.save(projectTopic);
    }

    @Override
    public Page<GetThesisResponse> filterTheses(ThesisFilterRequest filterRequest) {
        // Tạo specification từ filter request
        Specification<ProjectTopic> spec = ThesisSpecification.withFilter(filterRequest);
        
        // Tạo Pageable với sorting
        Sort sort = Sort.by(
            filterRequest.getSortDirection().equalsIgnoreCase("DESC") ? 
            Sort.Direction.DESC : Sort.Direction.ASC,
            filterRequest.getSortBy()
        );
        
        Pageable pageable = PageRequest.of(
            filterRequest.getPage(), 
            filterRequest.getSize(), 
            sort
        );
        
        // Thực hiện query với specification và pageable
        Page<ProjectTopic> thesisPage = projectTopicRepository.findAll(spec, pageable);
        
        // Map kết quả sang DTO
        return thesisPage.map(projectTopic -> {
            GetThesisResponse dto = projectTopicMapper.toResponse(projectTopic);
            
            // Set thêm thông tin suggestedBy và registerId nếu có
            dto.setSuggestedBy(
                projectTopic.getSuggestedTopics().stream()
                    .findFirst()
                    .map(SuggestedTopic::getSuggestedBy)
                    .orElse(null)
            );
            
            dto.setRegisterId(
                projectTopic.getRegisters().stream()
                    .findFirst()
                    .map(Register::getRegisterId)
                    .orElse(null)
            );
            
            return dto;
        });
    }

    @Override
    public Page<GetThesisResponse> filterThesesByQbe(ThesisQbeRequest request) {
        // Tạo probe từ request (chỉ set các trường scalar để QBE hoạt động đơn giản)
        ProjectTopic probe = ProjectTopic.builder()
                .topicCode(request.getTopicCode())
                .title(request.getTitle())
                .description(request.getDescription())
                .objectives(request.getObjectives())
                .methodology(request.getMethodology())
                .academicYearId(request.getAcademicYearId())
                .supervisorId(request.getSupervisorId())
                .difficultyLevel(request.getDifficultyLevel())
                .topicStatus(request.getTopicStatus())
                .approvalStatus(request.getApprovalStatus())
                .build();

        ExampleMatcher matcher = ExampleMatcher.matchingAll()
                .withIgnoreNullValues()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)
                .withIgnoreCase();

        Example<ProjectTopic> example = Example.of(probe, matcher);

        Sort sort = Sort.by(
                "DESC".equalsIgnoreCase(request.getSortDirection()) ? Sort.Direction.DESC : Sort.Direction.ASC,
                request.getSortBy()
        );
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);

        Page<ProjectTopic> page = projectTopicRepository.findAll(example, pageable);

        // Áp dụng lọc khoảng thời gian trong bộ nhớ (QBE không hỗ trợ range)
        List<ProjectTopic> filteredContent = page.getContent().stream()
                .filter(p -> request.getCreatedFrom() == null || (p.getCreatedAt() != null && !p.getCreatedAt().isBefore(request.getCreatedFrom())))
                .filter(p -> request.getCreatedTo() == null || (p.getCreatedAt() != null && !p.getCreatedAt().isAfter(request.getCreatedTo())))
                .filter(p -> request.getUpdatedFrom() == null || (p.getUpdatedAt() != null && !p.getUpdatedAt().isBefore(request.getUpdatedFrom())))
                .filter(p -> request.getUpdatedTo() == null || (p.getUpdatedAt() != null && !p.getUpdatedAt().isAfter(request.getUpdatedTo())))
                .collect(java.util.stream.Collectors.toList());

        // Giữ nguyên phân trang tổng thể từ DB; chỉ map phần nội dung đã lọc
        Page<ProjectTopic> adjustedPage = new org.springframework.data.domain.PageImpl<>(
                filteredContent,
                pageable,
                page.getTotalElements()
        );

        return adjustedPage.map(projectTopicMapper::toResponse);
    }

    @Override
    public List<GetThesisResponse> searchThesesByPattern(String searchPattern) {
        Specification<ProjectTopic> spec = ThesisSpecification.withSearchPattern(searchPattern);
        List<ProjectTopic> theses = projectTopicRepository.findAll(spec);
        return theses.stream()
                .map(projectTopicMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<GetThesisResponse> getThesesBySupervisor(Integer supervisorId) {
        Specification<ProjectTopic> spec = ThesisSpecification.withSupervisor(supervisorId);
        List<ProjectTopic> theses = projectTopicRepository.findAll(spec);
        return theses.stream()
                .map(projectTopicMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<GetThesisResponse> getThesesByAcademicYear(Integer academicYearId) {
        Specification<ProjectTopic> spec = ThesisSpecification.withAcademicYear(academicYearId);
        List<ProjectTopic> theses = projectTopicRepository.findAll(spec);
        return theses.stream()
                .map(projectTopicMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<GetThesisResponse> getThesesByDifficultyLevel(ProjectTopic.DifficultyLevel difficultyLevel) {
        Specification<ProjectTopic> spec = ThesisSpecification.withDifficultyLevel(difficultyLevel);
        List<ProjectTopic> theses = projectTopicRepository.findAll(spec);
        return theses.stream()
                .map(projectTopicMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<GetThesisResponse> getThesesByTopicStatus(ProjectTopic.TopicStatus topicStatus) {
        Specification<ProjectTopic> spec = ThesisSpecification.withTopicStatus(topicStatus);
        List<ProjectTopic> theses = projectTopicRepository.findAll(spec);
        return theses.stream()
                .map(projectTopicMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<GetThesisResponse> getThesesByApprovalStatus(ProjectTopic.ApprovalStatus approvalStatus) {
        Specification<ProjectTopic> spec = ThesisSpecification.withApprovalStatus(approvalStatus);
        List<ProjectTopic> theses = projectTopicRepository.findAll(spec);
        return theses.stream()
                .map(projectTopicMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<GetThesisResponse> getApprovedTopicsBySupervisor(Integer supervisorId) {
        List<ProjectTopic> approvedTopics = projectTopicRepository.findApprovedTopicsBySupervisor(supervisorId);
        return approvedTopics.stream()
                .map(projectTopic -> {
                    GetThesisResponse dto = projectTopicMapper.toResponse(projectTopic);
                    
                    // Set thêm thông tin suggestedBy và registerId nếu có
                    dto.setSuggestedBy(
                        projectTopic.getSuggestedTopics().stream()
                            .findFirst()
                            .map(SuggestedTopic::getSuggestedBy)
                            .orElse(null)
                    );
                    
                    dto.setRegisterId(
                        projectTopic.getRegisters().stream()
                            .findFirst()
                            .map(Register::getRegisterId)
                            .orElse(null)
                    );
                    
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public Page<GetThesisResponse> getApprovedTopicsBySupervisorWithPagination(Integer supervisorId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("topicId").descending());
        Page<ProjectTopic> approvedTopicsPage = projectTopicRepository.findBySupervisorIdAndApprovalStatus(
                supervisorId, 
                ProjectTopic.ApprovalStatus.APPROVED, 
                pageable
        );
        
        return approvedTopicsPage.map(projectTopic -> {
            GetThesisResponse dto = projectTopicMapper.toResponse(projectTopic);
            
            // Set thêm thông tin suggestedBy và registerId nếu có
            dto.setSuggestedBy(
                projectTopic.getSuggestedTopics().stream()
                    .findFirst()
                    .map(SuggestedTopic::getSuggestedBy)
                    .orElse(null)
            );
            
            dto.setRegisterId(
                projectTopic.getRegisters().stream()
                    .findFirst()
                    .map(Register::getRegisterId)
                    .orElse(null)
            );
            
            return dto;
        });
    }

    @Override
    public Long getApprovedTopicsCountBySupervisor(Integer supervisorId) {
        return projectTopicRepository.countApprovedTopicsBySupervisor(supervisorId);
    }

    @Override
    public Map<String, Object> getTopicStatusInfo(Integer topicId) {
        ProjectTopic projectTopic = projectTopicRepository.findById(topicId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project topic not found!"));
        
        Map<String, Object> statusInfo = new HashMap<>();
        statusInfo.put("topicId", projectTopic.getTopicId());
        statusInfo.put("title", projectTopic.getTitle());
        statusInfo.put("maxStudents", projectTopic.getMaxStudents());
        statusInfo.put("remainingSlots", projectTopic.getRemainingStudentSlots());
        statusInfo.put("acceptedStudents", projectTopic.getAcceptedStudentsCount());
        statusInfo.put("canAcceptMore", projectTopic.canAcceptMoreStudents());
        statusInfo.put("canBeApproved", projectTopic.canBeApproved());
        statusInfo.put("isFull", projectTopic.isFull());
        statusInfo.put("topicStatus", projectTopic.getTopicStatus());
        statusInfo.put("approvalStatus", projectTopic.getApprovalStatus());
        statusInfo.put("message", projectTopic.isFull() ? 
            "Đề tài đã đủ số lượng sinh viên" : 
            "Đề tài còn " + projectTopic.getRemainingStudentSlots() + " chỗ trống");
        
        return statusInfo;
    }

    @Override
    public boolean canTopicBeApproved(Integer topicId) {
        ProjectTopic projectTopic = projectTopicRepository.findById(topicId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project topic not found!"));
        
        return projectTopic.canBeApproved();
    }

    @Override
    public Map<String, Object> getSupervisorCapacityInfo(Integer supervisorId) {
        List<ProjectTopic> supervisorTopics = projectTopicRepository.findBySupervisorId(supervisorId, PageRequest.of(0, 1000))
                .getContent();
        
        int totalTopics = supervisorTopics.size();
        int activeTopics = (int) supervisorTopics.stream()
                .filter(topic -> topic.getTopicStatus() == ProjectTopic.TopicStatus.ACTIVE)
                .count();
        int approvedTopics = (int) supervisorTopics.stream()
                .filter(topic -> topic.getApprovalStatus() == ProjectTopic.ApprovalStatus.APPROVED)
                .count();
        int fullTopics = (int) supervisorTopics.stream()
                .filter(ProjectTopic::isFull)
                .count();
        
        // Tính toán chính xác hơn
        int totalInitialCapacity = totalTopics * 4; // Tổng sức chứa ban đầu
        int totalAcceptedStudents = supervisorTopics.stream()
                .mapToInt(ProjectTopic::getAcceptedStudentsCount)
                .sum();
        int totalRemainingSlots = totalInitialCapacity - totalAcceptedStudents; // Chỗ còn lại = Tổng ban đầu - Đã nhận
        
        // Tính tỷ lệ sử dụng (số sinh viên đã nhận / tổng sức chứa ban đầu)
        double utilizationRate = totalInitialCapacity > 0 ? 
            (double) totalAcceptedStudents / totalInitialCapacity * 100 : 0.0;
        
        // Tính tỷ lệ còn trống
        double remainingRate = totalInitialCapacity > 0 ? 
            (double) totalRemainingSlots / totalInitialCapacity * 100 : 0.0;
        
        Map<String, Object> capacityInfo = new HashMap<>();
        capacityInfo.put("supervisorId", supervisorId);
        capacityInfo.put("totalTopics", totalTopics);
        capacityInfo.put("activeTopics", activeTopics);
        capacityInfo.put("approvedTopics", approvedTopics);
        capacityInfo.put("fullTopics", fullTopics);
        capacityInfo.put("totalInitialCapacity", totalInitialCapacity);
        capacityInfo.put("totalAcceptedStudents", totalAcceptedStudents);
        capacityInfo.put("totalRemainingSlots", totalRemainingSlots);
        capacityInfo.put("utilizationRate", Math.round(utilizationRate * 100.0) / 100.0); // Làm tròn 2 chữ số thập phân
        capacityInfo.put("remainingRate", Math.round(remainingRate * 100.0) / 100.0);
        capacityInfo.put("message", "Thông tin năng lực giảng viên");
        
        return capacityInfo;
    }

    // ===================== Projection Implementations =====================
    @Override
    public org.springframework.data.domain.Page<ProjectTopicSummary> getTopicSummariesBySupervisor(Integer supervisorId, int page, int size) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        return projectTopicRepository.findSummariesBySupervisorIdAndApprovalStatus(
                supervisorId,
                ProjectTopic.ApprovalStatus.APPROVED,
                pageable
        );
    }

    @Override
    public org.springframework.data.domain.Page<ProjectTopicSummaryDto> getTopicSummaryDtosBySupervisor(Integer supervisorId, int page, int size) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        return projectTopicRepository.findSummaryDtosBySupervisorId(supervisorId, pageable);
    }

    @Override
    public <T> org.springframework.data.domain.Page<T> getTopicsByApprovalStatusWithProjection(ProjectTopic.ApprovalStatus status, Class<T> type, int page, int size) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        return projectTopicRepository.findByApprovalStatus(status, type, pageable);
    }
}
