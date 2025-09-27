package com.phenikaa.thesisservice.service.implement;

import com.phenikaa.dto.response.GetUserResponse;
import com.phenikaa.thesisservice.client.UserServiceClient;
import com.phenikaa.thesisservice.dto.response.GetStudentPeriodResponse;
import com.phenikaa.thesisservice.entity.Register;
import com.phenikaa.thesisservice.entity.SuggestedTopic;
import com.phenikaa.thesisservice.repository.RegisterRepository;
import com.phenikaa.thesisservice.repository.SuggestRepository;
import com.phenikaa.thesisservice.service.interfaces.StudentPeriodService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudentPeriodServiceImpl implements StudentPeriodService {

    private final RegisterRepository registerRepository;
    private final SuggestRepository suggestRepository;
    private final UserServiceClient userServiceClient;

    @Override
    public List<GetStudentPeriodResponse> getStudentsByPeriod(Integer periodId) {
        log.info("Lấy danh sách sinh viên đã đăng ký đề tài theo đợt: {}", periodId);
        
        List<Register> registrations = registerRepository.findByRegistrationPeriodId(periodId);
        
        return registrations.stream()
                .map(this::mapRegisterToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<GetStudentPeriodResponse> getSuggestedStudentsByPeriod(Integer periodId) {
        log.info("Lấy danh sách sinh viên đã đề xuất đề tài theo đợt: {}", periodId);
        
        List<SuggestedTopic> suggestedTopics = suggestRepository.findByRegistrationPeriodId(periodId);
        
        return suggestedTopics.stream()
                .map(this::mapSuggestedTopicToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<GetStudentPeriodResponse> getAllStudentsByPeriod(Integer periodId) {
        log.info("Lấy danh sách tất cả sinh viên theo đợt: {}", periodId);
        
        List<GetStudentPeriodResponse> registeredStudents = getStudentsByPeriod(periodId);
        List<GetStudentPeriodResponse> suggestedStudents = getSuggestedStudentsByPeriod(periodId);
        
        // Kết hợp cả hai danh sách
        registeredStudents.addAll(suggestedStudents);
        
        log.info("Tổng số sinh viên tìm thấy: {} (đăng ký: {}, đề xuất: {})", 
                registeredStudents.size(), 
                getStudentsByPeriod(periodId).size(), 
                suggestedStudents.size());
        
        return registeredStudents;
    }

    /**
     * Map Register entity sang GetStudentPeriodResponse
     */
    private GetStudentPeriodResponse mapRegisterToResponse(Register register) {
        String username = getUsernameById(register.getStudentId());
        String fullName = getFullNameById(register.getStudentId());
        Integer supervisorId = register.getProjectTopic() != null ? register.getProjectTopic().getSupervisorId() : null;
        String supervisorFullName = supervisorId != null ? getFullNameById(supervisorId) : null;
        
        return GetStudentPeriodResponse.builder()
                .registrationPeriodId(register.getRegistrationPeriodId())
                .studentId(register.getStudentId())
                .username(username)
                .fullName(fullName)
                .supervisorId(supervisorId)
                .supervisorFullName(supervisorFullName)
                .suggestionStatus(null) // Register không có suggestion status
                .registrationType("REGISTERED")
                .topicId(register.getProjectTopic() != null ? register.getProjectTopic().getTopicId() : null)
                .topicTitle(register.getProjectTopic() != null ? register.getProjectTopic().getTitle() : null)
                .topicCode(register.getProjectTopic() != null ? register.getProjectTopic().getTopicCode() : null)
                .build();
    }

    /**
     * Map SuggestedTopic entity sang GetStudentPeriodResponse
     */
    private GetStudentPeriodResponse mapSuggestedTopicToResponse(SuggestedTopic suggestedTopic) {
        String username = getUsernameById(suggestedTopic.getSuggestedBy());
        String fullName = getFullNameById(suggestedTopic.getSuggestedBy());
        Integer supervisorId = suggestedTopic.getSuggestedFor();
        String supervisorFullName = supervisorId != null ? getFullNameById(supervisorId) : null;
        
        return GetStudentPeriodResponse.builder()
                .registrationPeriodId(suggestedTopic.getRegistrationPeriodId())
                .studentId(suggestedTopic.getSuggestedBy())
                .username(username)
                .fullName(fullName)
                .supervisorId(supervisorId)
                .supervisorFullName(supervisorFullName)
                .suggestionStatus(suggestedTopic.getSuggestionStatus())
                .registrationType("SUGGESTED")
                .topicId(suggestedTopic.getProjectTopic() != null ? suggestedTopic.getProjectTopic().getTopicId() : null)
                .topicTitle(suggestedTopic.getProjectTopic() != null ? suggestedTopic.getProjectTopic().getTitle() : null)
                .topicCode(suggestedTopic.getProjectTopic() != null ? suggestedTopic.getProjectTopic().getTopicCode() : null)
                .build();
    }

    /**
     * Lấy username từ user-service
     */
    private String getUsernameById(Integer userId) {
        try {
            log.debug("Đang lấy username cho userId: {}", userId);
            
            // Thử endpoint get-profile trước
            try {
                GetUserResponse user = userServiceClient.getUserById(userId);
                if (user != null && user.getUsername() != null) {
                    log.debug("Tìm thấy user: {} với username: {}", user.getUserId(), user.getUsername());
                    return user.getUsername();
                }
            } catch (Exception e) {
                log.warn("Endpoint get-profile thất bại cho userId {}: {}", userId, e.getMessage());
            }
            
            // Fallback: thử endpoint get-username
            try {
                String username = userServiceClient.getUsernameById(userId);
                if (username != null && !username.isEmpty()) {
                    log.debug("Tìm thấy username qua endpoint get-username: {}", username);
                    return username;
                }
            } catch (Exception e) {
                log.warn("Endpoint get-username thất bại cho userId {}: {}", userId, e.getMessage());
            }
            
            log.warn("Không thể lấy username cho userId: {}", userId);
            return null;
        } catch (Exception e) {
            log.error("Lỗi khi lấy username cho userId {}: {}", userId, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Lấy fullName từ user-service
     */
    private String getFullNameById(Integer userId) {
        try {
            log.debug("Đang lấy fullName cho userId: {}", userId);
            
            GetUserResponse user = userServiceClient.getUserById(userId);
            if (user != null && user.getFullName() != null && !user.getFullName().trim().isEmpty()) {
                log.debug("Tìm thấy fullName: {}", user.getFullName());
                return user.getFullName();
            }
            
            log.warn("Không thể lấy fullName cho userId: {}", userId);
            return null;
        } catch (Exception e) {
            log.error("Lỗi khi lấy fullName cho userId {}: {}", userId, e.getMessage(), e);
            return null;
        }
    }
}
