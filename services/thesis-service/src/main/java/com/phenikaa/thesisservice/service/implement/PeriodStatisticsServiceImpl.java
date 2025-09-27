package com.phenikaa.thesisservice.service.implement;

import com.phenikaa.thesisservice.repository.RegisterRepository;
import com.phenikaa.thesisservice.repository.SuggestRepository;
import com.phenikaa.thesisservice.repository.RegistrationPeriodRepository;
import com.phenikaa.thesisservice.service.PeriodStatisticsService;
import com.phenikaa.thesisservice.client.UserServiceClient;
import com.phenikaa.dto.response.GetUserResponse;
import com.phenikaa.thesisservice.entity.Register;
import com.phenikaa.thesisservice.entity.SuggestedTopic;
import com.phenikaa.thesisservice.entity.RegistrationPeriod;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class PeriodStatisticsServiceImpl implements PeriodStatisticsService {

    private final RegisterRepository registerRepository;
    private final SuggestRepository suggestRepository;
    private final RegistrationPeriodRepository registrationPeriodRepository;
    private final UserServiceClient userServiceClient;

    public PeriodStatisticsServiceImpl(RegisterRepository registerRepository,
                                     SuggestRepository suggestRepository,
                                     RegistrationPeriodRepository registrationPeriodRepository,
                                     UserServiceClient userServiceClient) {
        this.registerRepository = registerRepository;
        this.suggestRepository = suggestRepository;
        this.registrationPeriodRepository = registrationPeriodRepository;
        this.userServiceClient = userServiceClient;
    }

    @Override
    public Map<String, Object> getPeriodOverview(Integer periodId) {
        // Lấy thông tin đợt đăng ký
        Optional<RegistrationPeriod> periodOpt = registrationPeriodRepository.findById(periodId);
        if (periodOpt.isEmpty()) {
            throw new IllegalArgumentException("Đợt đăng ký không tồn tại");
        }

        RegistrationPeriod period = periodOpt.get();
        
        // Lấy các thống kê
        Integer registeredStudents = getRegisteredStudents(periodId);
        Integer unregisteredStudents = getUnregisteredStudents(periodId);
        Integer totalStudents = registeredStudents + unregisteredStudents;
        
        // Tính tỷ lệ đăng ký
        Double registrationRate = 0.0;
        if (totalStudents > 0) {
            registrationRate = (double) registeredStudents / totalStudents * 100;
        }

        Map<String, Object> response = new HashMap<>();
        response.put("periodId", periodId);
        response.put("periodName", period.getPeriodName());
        response.put("totalStudents", totalStudents);
        response.put("registeredStudents", registeredStudents);
        response.put("unregisteredStudents", unregisteredStudents);
        response.put("registrationRate", Math.round(registrationRate * 100.0) / 100.0);
        
        return response;
    }

    @Override
    public Integer getTotalStudents(Integer periodId) {
        // Tổng số sinh viên = đã đăng ký + chưa đăng ký
        return getRegisteredStudents(periodId) + getUnregisteredStudents(periodId);
    }

    @Override
    public Integer getRegisteredStudents(Integer periodId) {
        // Lấy sinh viên đã đăng ký đề tài
        List<Register> registrations = registerRepository.findByRegistrationPeriodId(periodId);
        Set<Integer> registeredStudentIds = registrations.stream()
            .map(Register::getStudentId)
            .collect(Collectors.toSet());
            
        // Lấy sinh viên đã đề xuất đề tài
        List<SuggestedTopic> suggestions = suggestRepository.findByRegistrationPeriodId(periodId);
        Set<Integer> suggestedStudentIds = suggestions.stream()
            .map(SuggestedTopic::getSuggestedBy)
            .collect(Collectors.toSet());
        
        // Hợp nhất 2 tập hợp (loại bỏ trùng lặp)
        Set<Integer> allRegisteredStudentIds = new HashSet<>();
        allRegisteredStudentIds.addAll(registeredStudentIds);
        allRegisteredStudentIds.addAll(suggestedStudentIds);
        
        return allRegisteredStudentIds.size();
    }

    @Override
    public Integer getUnregisteredStudents(Integer periodId) {
        try {
            // Lấy tất cả sinh viên trong hệ thống
            List<GetUserResponse> allStudents = userServiceClient.getUsersByRole("STUDENT");
            
            // Lấy danh sách sinh viên đã đăng ký/đề xuất
            Integer registeredCount = getRegisteredStudents(periodId);
            
            // Số sinh viên chưa đăng ký = tổng - đã đăng ký
            return allStudents.size() - registeredCount;
            
        } catch (Exception e) {
            throw new IllegalStateException("Lỗi khi lấy danh sách sinh viên: " + e.getMessage());
        }
    }
}
