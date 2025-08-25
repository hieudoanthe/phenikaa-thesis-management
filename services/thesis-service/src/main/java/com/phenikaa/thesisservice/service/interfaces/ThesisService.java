package com.phenikaa.thesisservice.service.interfaces;

import com.phenikaa.thesisservice.dto.request.CreateProjectTopicRequest;
import com.phenikaa.thesisservice.dto.request.EditProjectTopicRequest;
import com.phenikaa.thesisservice.dto.request.UpdateProjectTopicRequest;
import com.phenikaa.thesisservice.dto.request.ThesisFilterRequest;
import com.phenikaa.thesisservice.dto.response.AvailableTopicResponse;
import com.phenikaa.thesisservice.dto.response.GetThesisResponse;
import com.phenikaa.thesisservice.entity.ProjectTopic;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface ThesisService {
    ProjectTopic createProjectTopic(CreateProjectTopicRequest dto, Integer userId);
    List<GetThesisResponse> findAll();
    Page<GetThesisResponse> getTopicsByTeacherId(Integer teacherId, int page, int size);
    ProjectTopic editProjectTopic(EditProjectTopicRequest dto);
    ProjectTopic updateProjectTopic(UpdateProjectTopicRequest dto);
    void deleteTopic(Integer topicId);
    List<AvailableTopicResponse> getAvailableTopics();
    void approvedTopic(Integer topicId);
    void rejectTopic(Integer topicId);
    
    // Thêm method filter theo specification
    Page<GetThesisResponse> filterTheses(ThesisFilterRequest filterRequest);
    
    // Các method filter đơn giản
    List<GetThesisResponse> searchThesesByPattern(String searchPattern);
    List<GetThesisResponse> getThesesBySupervisor(Integer supervisorId);
    List<GetThesisResponse> getThesesByAcademicYear(Integer academicYearId);
    List<GetThesisResponse> getThesesByDifficultyLevel(ProjectTopic.DifficultyLevel difficultyLevel);
    List<GetThesisResponse> getThesesByTopicStatus(ProjectTopic.TopicStatus topicStatus);
    List<GetThesisResponse> getThesesByApprovalStatus(ProjectTopic.ApprovalStatus approvalStatus);
    
    // Thêm method để lấy đề tài đã được xác nhận bởi giảng viên
    List<GetThesisResponse> getApprovedTopicsBySupervisor(Integer supervisorId);
    Page<GetThesisResponse> getApprovedTopicsBySupervisorWithPagination(Integer supervisorId, int page, int size);
    Long getApprovedTopicsCountBySupervisor(Integer supervisorId);
    
    // Thêm method để kiểm tra trạng thái đề tài
    Map<String, Object> getTopicStatusInfo(Integer topicId);
    boolean canTopicBeApproved(Integer topicId);
    Map<String, Object> getSupervisorCapacityInfo(Integer supervisorId);
}
