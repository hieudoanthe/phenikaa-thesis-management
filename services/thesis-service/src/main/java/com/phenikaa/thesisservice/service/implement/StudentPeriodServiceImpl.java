package com.phenikaa.thesisservice.service.implement;

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
        return GetStudentPeriodResponse.builder()
                .registrationPeriodId(register.getRegistrationPeriodId())
                .studentId(register.getStudentId())
                .supervisorId(register.getProjectTopic() != null ? register.getProjectTopic().getSupervisorId() : null)
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
        return GetStudentPeriodResponse.builder()
                .registrationPeriodId(suggestedTopic.getRegistrationPeriodId())
                .studentId(suggestedTopic.getSuggestedBy())
                .supervisorId(suggestedTopic.getSuggestedFor())
                .suggestionStatus(suggestedTopic.getSuggestionStatus())
                .registrationType("SUGGESTED")
                .topicId(suggestedTopic.getProjectTopic() != null ? suggestedTopic.getProjectTopic().getTopicId() : null)
                .topicTitle(suggestedTopic.getProjectTopic() != null ? suggestedTopic.getProjectTopic().getTitle() : null)
                .topicCode(suggestedTopic.getProjectTopic() != null ? suggestedTopic.getProjectTopic().getTopicCode() : null)
                .build();
    }
}
