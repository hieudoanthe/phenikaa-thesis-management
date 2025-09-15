package com.phenikaa.thesisservice.service.interfaces;

import com.phenikaa.thesisservice.dto.request.CreateProjectTopicRequest;
import com.phenikaa.thesisservice.dto.request.EditProjectTopicRequest;
import com.phenikaa.thesisservice.dto.request.UpdateProjectTopicRequest;
import com.phenikaa.thesisservice.dto.request.ThesisSpecificationFilterRequest;
import com.phenikaa.thesisservice.dto.request.ThesisQbeFilterRequest;
import com.phenikaa.thesisservice.dto.response.AvailableTopicResponse;
import com.phenikaa.thesisservice.dto.response.GetThesisResponse;
import com.phenikaa.thesisservice.entity.ProjectTopic;
import com.phenikaa.thesisservice.projection.ProjectTopicSummary;
import com.phenikaa.thesisservice.dto.response.ProjectTopicSummaryDto;
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

    Page<GetThesisResponse> filterTheses(ThesisSpecificationFilterRequest filterRequest);

    Page<GetThesisResponse> filterThesesByQbe(ThesisQbeFilterRequest request);

    List<GetThesisResponse> searchThesesByPattern(String searchPattern);
    List<GetThesisResponse> getThesesBySupervisor(Integer supervisorId);
    List<GetThesisResponse> getThesesByAcademicYear(Integer academicYearId);
    List<GetThesisResponse> getThesesByDifficultyLevel(ProjectTopic.DifficultyLevel difficultyLevel);
    List<GetThesisResponse> getThesesByTopicStatus(ProjectTopic.TopicStatus topicStatus);
    List<GetThesisResponse> getThesesByApprovalStatus(ProjectTopic.ApprovalStatus approvalStatus);

    Page<GetThesisResponse> getApprovedTopicsBySupervisorWithPagination(Integer supervisorId, int page, int size);
    Long getApprovedTopicsCountBySupervisor(Integer supervisorId);

    Map<String, Object> getTopicStatusInfo(Integer topicId);
    boolean canTopicBeApproved(Integer topicId);
    Map<String, Object> getSupervisorCapacityInfo(Integer supervisorId);
    Map<String, Object> getTopicById(Integer topicId);

    // Statistics methods
    Long getTopicCount();
    Long getTopicCountByStatus(String status);
    Long getTopicCountByDifficulty(String difficulty);
    Long getTopicCountByAcademicYear(Integer academicYearId);
    Long getTopicCountBySupervisor(Integer supervisorId);
    List<Map<String, Object>> getTopicsBySupervisor(Integer supervisorId);
    Map<String, Object> getTopicsStatsBySupervisor(Integer supervisorId);
    List<Map<String, Object>> getTopicsOverTime(String startDate, String endDate);

//    Page<ProjectTopicSummary> getTopicSummariesBySupervisor(Integer supervisorId, int page, int size);
//    Page<ProjectTopicSummaryDto> getTopicSummaryDtosBySupervisor(Integer supervisorId, int page, int size);
//    <T> Page<T> getTopicsByApprovalStatusWithProjection(ProjectTopic.ApprovalStatus status, Class<T> type, int page, int size);
}
