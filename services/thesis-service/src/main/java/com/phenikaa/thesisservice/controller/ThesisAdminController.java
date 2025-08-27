package com.phenikaa.thesisservice.controller;

import com.phenikaa.thesisservice.dto.request.EditProjectTopicRequest;
import com.phenikaa.thesisservice.dto.request.ThesisFilterRequest;
import com.phenikaa.thesisservice.dto.response.GetThesisResponse;
import com.phenikaa.thesisservice.dto.request.ThesisQbeRequest;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.HashMap;


@RestController
@RequestMapping("/api/thesis-service/teacher")
@RequiredArgsConstructor
public class ThesisAdminController {

    private final ThesisService thesisService;
    private final JwtUtil jwtUtil;

    @PreAuthorize("hasRole('TEACHER')")
    @PostMapping("/create-topic")
    public ProjectTopic createTopic(@RequestHeader("Authorization") String token,
                                    @RequestBody CreateProjectTopicRequest request) {
        Integer userId = jwtUtil.extractUserId(token);
        return thesisService.createProjectTopic(request, userId);
    }

    @PreAuthorize("hasRole('TEACHER')")
    @GetMapping("/get-list-topic")
    public List<GetThesisResponse> getListTopic() {
        return thesisService.findAll();
    }

    // ========================= Projection test endpoints =========================
    @PreAuthorize("hasRole('TEACHER')")
    @GetMapping("/projection/interface/supervisor/{supervisorId}")
    public ResponseEntity<Page<ProjectTopicSummary>> getInterfaceProjections(
            @PathVariable Integer supervisorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<ProjectTopicSummary> result = thesisService.getTopicSummariesBySupervisor(supervisorId, page, size);
        return ResponseEntity.ok(result);
    }

    @PreAuthorize("hasRole('TEACHER')")
    @GetMapping("/projection/dto/supervisor/{supervisorId}")
    public ResponseEntity<Page<ProjectTopicSummaryDto>> getDtoProjections(
            @PathVariable Integer supervisorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<ProjectTopicSummaryDto> result = thesisService.getTopicSummaryDtosBySupervisor(supervisorId, page, size);
        return ResponseEntity.ok(result);
    }

    @PreAuthorize("hasRole('TEACHER')")
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

    @PreAuthorize("hasRole('TEACHER')")
    @PostMapping("/edit-topic")
    public ProjectTopic editTopic(@RequestBody EditProjectTopicRequest request) {
        return thesisService.editProjectTopic(request);
    }

    @PutMapping("update-topic")
    public ResponseEntity<ProjectTopic> updateTopic(@RequestBody UpdateProjectTopicRequest request) {
        ProjectTopic updatedTopic = thesisService.updateProjectTopic(request);
        return ResponseEntity.ok(updatedTopic);
    }

    @PreAuthorize("hasRole('TEACHER')")
    @DeleteMapping("/delete-topic")
    public void deleteTopic(@RequestParam Integer topicId) {
        thesisService.deleteTopic(topicId);
    }

    @PreAuthorize("hasRole('TEACHER')")
    @PatchMapping("/approve-topic")
    public ResponseEntity<Void> approveTopic(@RequestParam Integer topicId) {
        thesisService.approvedTopic(topicId);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('TEACHER')")
    @PatchMapping("/reject-topic")
    public ResponseEntity<Void> rejectTopic(@RequestParam Integer topicId) {
        thesisService.rejectTopic(topicId);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('TEACHER')")
    @PostMapping("/filter-theses")
    public ResponseEntity<ThesisFilterResponse> filterTheses(@RequestBody ThesisFilterRequest filterRequest) {
        Page<GetThesisResponse> filteredTheses = thesisService.filterTheses(filterRequest);
        ThesisFilterResponse response = ThesisFilterResponse.fromPage(filteredTheses);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('TEACHER')")
    @PostMapping("/filter-theses-qbe")
    public ResponseEntity<ThesisFilterResponse> filterThesesByQbe(@RequestBody ThesisQbeRequest request) {
        Page<GetThesisResponse> filteredTheses = thesisService.filterThesesByQbe(request);
        ThesisFilterResponse response = ThesisFilterResponse.fromPage(filteredTheses);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('TEACHER')")
    @GetMapping("/search-theses")
    public ResponseEntity<List<GetThesisResponse>> searchTheses(@RequestParam String searchPattern) {
        List<GetThesisResponse> theses = thesisService.searchThesesByPattern(searchPattern);
        return ResponseEntity.ok(theses);
    }

    @PreAuthorize("hasRole('TEACHER')")
    @GetMapping("/theses-by-supervisor")
    public ResponseEntity<List<GetThesisResponse>> getThesesBySupervisor(@RequestParam Integer supervisorId) {
        List<GetThesisResponse> theses = thesisService.getThesesBySupervisor(supervisorId);
        return ResponseEntity.ok(theses);
    }

    @PreAuthorize("hasRole('TEACHER')")
    @GetMapping("/theses-by-academic-year")
    public ResponseEntity<List<GetThesisResponse>> getThesesByAcademicYear(@RequestParam Integer academicYearId) {
        List<GetThesisResponse> theses = thesisService.getThesesByAcademicYear(academicYearId);
        return ResponseEntity.ok(theses);
    }

    @PreAuthorize("hasRole('TEACHER')")
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

    @PreAuthorize("hasRole('TEACHER')")
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

    @PreAuthorize("hasRole('TEACHER')")
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

    /**
     * Lấy danh sách đề tài đã được xác nhận bởi giảng viên 
     */
    @PreAuthorize("hasRole('TEACHER')")
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

    /**
     * Lấy số lượng đề tài đã được xác nhận bởi giảng viên
     */
    @PreAuthorize("hasRole('TEACHER')")
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

    /**
     * Lấy số lượng đề tài đã được xác nhận bởi giảng viên cụ thể
     */
    @PreAuthorize("hasRole('TEACHER')")
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

    /**
     * Lấy thông tin trạng thái chi tiết của một đề tài
     */
    @PreAuthorize("hasRole('TEACHER')")
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

    /**
     * Kiểm tra xem đề tài có thể được approve không
     */
    @PreAuthorize("hasRole('TEACHER')")
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

    /**
     * Lấy thông tin năng lực của giảng viên (số lượng đề tài, sinh viên đã nhận, etc.)
     */
    @PreAuthorize("hasRole('TEACHER')")
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

    /**
     * Lấy thông tin năng lực của giảng viên cụ thể
     */
    @PreAuthorize("hasRole('TEACHER')")
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

    /**
     * Lấy danh sách đề tài đã được xác nhận bởi giảng viên cụ thể 
     */
    @PreAuthorize("hasRole('TEACHER')")
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

