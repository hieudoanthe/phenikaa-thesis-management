package com.phenikaa.thesisservice.controller;

import com.phenikaa.thesisservice.dto.request.EditProjectTopicRequest;
import com.phenikaa.thesisservice.dto.request.ThesisSpecificationFilterRequest;
import com.phenikaa.thesisservice.dto.response.GetThesisResponse;
import com.phenikaa.thesisservice.dto.request.ThesisQbeFilterRequest;
import com.phenikaa.thesisservice.dto.response.ThesisFilterResponse;
import com.phenikaa.thesisservice.dto.response.ProjectTopicSummaryDto;
import com.phenikaa.thesisservice.service.interfaces.ThesisService;
import com.phenikaa.thesisservice.projection.ProjectTopicSummary;
import com.phenikaa.utils.JwtUtil;
import com.phenikaa.thesisservice.dto.request.CreateProjectTopicRequest;
import com.phenikaa.thesisservice.dto.request.UpdateProjectTopicRequest;
import com.phenikaa.thesisservice.entity.ProjectTopic;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.HashMap;


@RestController
@RequestMapping("/api/thesis-service/teacher")
@RequiredArgsConstructor
public class ThesisTeacherController {

    private final ThesisService thesisService;
    private final JwtUtil jwtUtil;

    @PostMapping("/create-topic")
    public ProjectTopic createTopic(@RequestHeader("Authorization") String token,
                                    @RequestBody CreateProjectTopicRequest request) {
        Integer userId = jwtUtil.extractUserId(token);
        return thesisService.createProjectTopic(request, userId);
    }

    @GetMapping("/get-list-topic")
    public List<GetThesisResponse> getListTopic() {
        return thesisService.findAll();
    }

    @GetMapping("/projection/interface/supervisor/{supervisorId}")
    public ResponseEntity<Page<ProjectTopicSummary>> getInterfaceProjections(
            @PathVariable Integer supervisorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<ProjectTopicSummary> result = thesisService.getTopicSummariesBySupervisor(supervisorId, page, size);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/projection/dto/supervisor/{supervisorId}")
    public ResponseEntity<Page<ProjectTopicSummaryDto>> getDtoProjections(
            @PathVariable Integer supervisorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<ProjectTopicSummaryDto> result = thesisService.getTopicSummaryDtosBySupervisor(supervisorId, page, size);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/projection/dynamic/approval/{approvalStatus}")
    public ResponseEntity<Page<ProjectTopicSummary>> getDynamicProjections(
            @PathVariable String approvalStatus,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            ProjectTopic.ApprovalStatus status = ProjectTopic.ApprovalStatus.valueOf(approvalStatus.toUpperCase());
            Page<ProjectTopicSummary> result = thesisService.getTopicsByApprovalStatusWithProjection(status, ProjectTopicSummary.class, page, size);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/get-topic-by-{teacherId}/paged")
    public ResponseEntity<Page<GetThesisResponse>> getTopicsByTeacherId(
            @PathVariable Integer teacherId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size) {

        Page<GetThesisResponse> topics =
                thesisService.getTopicsByTeacherId(teacherId, page, size);

        return ResponseEntity.ok(topics);
    }

    @PostMapping("/edit-topic")
    public ProjectTopic editTopic(@RequestBody EditProjectTopicRequest request) {
        return thesisService.editProjectTopic(request);
    }

    @PutMapping("update-topic")
    public ResponseEntity<ProjectTopic> updateTopic(@RequestBody UpdateProjectTopicRequest request) {
        ProjectTopic updatedTopic = thesisService.updateProjectTopic(request);
        return ResponseEntity.ok(updatedTopic);
    }

    @DeleteMapping("/delete-topic")
    public void deleteTopic(@RequestParam Integer topicId) {
        thesisService.deleteTopic(topicId);
    }

    @PatchMapping("/approve-topic")
    public ResponseEntity<Void> approveTopic(@RequestParam Integer topicId) {
        thesisService.approvedTopic(topicId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/reject-topic")
    public ResponseEntity<Void> rejectTopic(@RequestParam Integer topicId) {
        thesisService.rejectTopic(topicId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/filter-theses")
    public ResponseEntity<ThesisFilterResponse> filterTheses(@RequestBody ThesisSpecificationFilterRequest filterRequest) {
        Page<GetThesisResponse> filteredTheses = thesisService.filterTheses(filterRequest);
        ThesisFilterResponse response = ThesisFilterResponse.fromPage(filteredTheses);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/filter-theses-qbe")
    public ResponseEntity<ThesisFilterResponse> filterThesesByQbe(@RequestBody ThesisQbeFilterRequest request) {
        Page<GetThesisResponse> filteredTheses = thesisService.filterThesesByQbe(request);
        ThesisFilterResponse response = ThesisFilterResponse.fromPage(filteredTheses);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search-theses")
    public ResponseEntity<List<GetThesisResponse>> searchTheses(@RequestParam String searchPattern) {
        List<GetThesisResponse> theses = thesisService.searchThesesByPattern(searchPattern);
        return ResponseEntity.ok(theses);
    }

    @GetMapping("/theses-by-supervisor")
    public ResponseEntity<List<GetThesisResponse>> getThesesBySupervisor(@RequestParam Integer supervisorId) {
        List<GetThesisResponse> theses = thesisService.getThesesBySupervisor(supervisorId);
        return ResponseEntity.ok(theses);
    }

    @GetMapping("/theses-by-academic-year")
    public ResponseEntity<List<GetThesisResponse>> getThesesByAcademicYear(@RequestParam Integer academicYearId) {
        List<GetThesisResponse> theses = thesisService.getThesesByAcademicYear(academicYearId);
        return ResponseEntity.ok(theses);
    }

    @GetMapping("/theses-by-difficulty")
    public ResponseEntity<List<GetThesisResponse>> getThesesByDifficultyLevel(
            @RequestParam String difficultyLevel) {
        try {
            ProjectTopic.DifficultyLevel level = ProjectTopic.DifficultyLevel.valueOf(difficultyLevel.toUpperCase());
            List<GetThesisResponse> theses = thesisService.getThesesByDifficultyLevel(level);
            return ResponseEntity.ok(theses);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/theses-by-topic-status")
    public ResponseEntity<List<GetThesisResponse>> getThesesByTopicStatus(
            @RequestParam String topicStatus) {
        try {
            ProjectTopic.TopicStatus status = ProjectTopic.TopicStatus.valueOf(topicStatus.toUpperCase());
            List<GetThesisResponse> theses = thesisService.getThesesByTopicStatus(status);
            return ResponseEntity.ok(theses);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/theses-by-approval-status")
    public ResponseEntity<List<GetThesisResponse>> getThesesByApprovalStatus(
            @RequestParam String approvalStatus) {
        try {
            ProjectTopic.ApprovalStatus status = ProjectTopic.ApprovalStatus.valueOf(approvalStatus.toUpperCase());
            List<GetThesisResponse> theses = thesisService.getThesesByApprovalStatus(status);
            return ResponseEntity.ok(theses);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/approved-topics/paged")
    public ResponseEntity<Page<GetThesisResponse>> getApprovedTopicsWithPagination(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Integer userId = jwtUtil.extractUserId(token);
            Page<GetThesisResponse> approvedTopics = thesisService.getApprovedTopicsBySupervisorWithPagination(userId, page, size);
            return ResponseEntity.ok(approvedTopics);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/approved-topics/count")
    public ResponseEntity<Map<String, Object>> getApprovedTopicsCount(@RequestHeader("Authorization") String token) {
        try {
            Integer userId = jwtUtil.extractUserId(token);
            Long count = thesisService.getApprovedTopicsCountBySupervisor(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("supervisorId", userId);
            response.put("approvedTopicsCount", count);
            response.put("message", "Số lượng đề tài đã được xác nhận");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Không thể lấy số lượng đề tài đã xác nhận"));
        }
    }

    @GetMapping("/approved-topics/supervisor/{supervisorId}/count")
    public ResponseEntity<Map<String, Object>> getApprovedTopicsCountBySpecificSupervisor(
            @PathVariable Integer supervisorId) {
        try {
            Long count = thesisService.getApprovedTopicsCountBySupervisor(supervisorId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("supervisorId", supervisorId);
            response.put("approvedTopicsCount", count);
            response.put("message", "Số lượng đề tài đã được xác nhận bởi giảng viên");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Không thể lấy số lượng đề tài đã xác nhận"));
        }
    }

    // Lấy trạng thái hiện tại của đề tài
    @GetMapping("/topic-status/{topicId}")
    public ResponseEntity<Map<String, Object>> getTopicStatusInfo(@PathVariable Integer topicId) {
        try {
            Map<String, Object> statusInfo = thesisService.getTopicStatusInfo(topicId);
            return ResponseEntity.ok(statusInfo);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body(Map.of("error", e.getReason()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Không thể lấy thông tin trạng thái đề tài"));
        }
    }

    @GetMapping("/topic-can-approve/{topicId}")
    public ResponseEntity<Map<String, Object>> checkTopicCanBeApproved(@PathVariable Integer topicId) {
        try {
            boolean canBeApproved = thesisService.canTopicBeApproved(topicId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("topicId", topicId);
            response.put("canBeApproved", canBeApproved);
            response.put("message", canBeApproved ? 
                "Đề tài có thể được approve" : 
                "Đề tài không thể được approve");
            
            return ResponseEntity.ok(response);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body(Map.of("error", e.getReason()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Không thể kiểm tra trạng thái đề tài"));
        }
    }

    // Lấy thông tin năng lực của giảng viên
    @GetMapping("/supervisor-capacity")
    public ResponseEntity<Map<String, Object>> getSupervisorCapacity(@RequestHeader("Authorization") String token) {
        try {
            Integer userId = jwtUtil.extractUserId(token);
            Map<String, Object> capacityInfo = thesisService.getSupervisorCapacityInfo(userId);
            return ResponseEntity.ok(capacityInfo);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Không thể lấy thông tin năng lực giảng viên"));
        }
    }

    // Lấy thông tin năng lực của giảng viên cụ thể
    @GetMapping("/supervisor-capacity/{supervisorId}")
    public ResponseEntity<Map<String, Object>> getSpecificSupervisorCapacity(@PathVariable Integer supervisorId) {
        try {
            Map<String, Object> capacityInfo = thesisService.getSupervisorCapacityInfo(supervisorId);
            return ResponseEntity.ok(capacityInfo);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Không thể lấy thông tin năng lực giảng viên"));
        }
    }

    @GetMapping("/approved-topics/supervisor/{supervisorId}/paged")
    public ResponseEntity<Page<GetThesisResponse>> getApprovedTopicsBySpecificSupervisorWithPagination(
            @PathVariable Integer supervisorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<GetThesisResponse> approvedTopics = thesisService.getApprovedTopicsBySupervisorWithPagination(supervisorId, page, size);
            return ResponseEntity.ok(approvedTopics);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

