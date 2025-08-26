package com.phenikaa.thesisservice.specification;

import com.phenikaa.thesisservice.dto.request.ThesisFilterRequest;
import com.phenikaa.thesisservice.entity.ProjectTopic;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import jakarta.persistence.criteria.Predicate;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class ThesisSpecification {
    
    /**
     * Tạo specification từ filter request
     */
    public static Specification<ProjectTopic> withFilter(ThesisFilterRequest filterRequest) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // Lọc theo topic code
            if (StringUtils.hasText(filterRequest.getTopicCode())) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("topicCode")),
                    "%" + filterRequest.getTopicCode().toLowerCase() + "%"
                ));
            }
            
            // Lọc theo title
            if (StringUtils.hasText(filterRequest.getTitle())) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("title")),
                    "%" + filterRequest.getTitle().toLowerCase() + "%"
                ));
            }
            
            // Lọc theo description
            if (StringUtils.hasText(filterRequest.getDescription())) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("description")),
                    "%" + filterRequest.getDescription().toLowerCase() + "%"
                ));
            }
            
            // Lọc theo objectives
            if (StringUtils.hasText(filterRequest.getObjectives())) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("objectives")),
                    "%" + filterRequest.getObjectives().toLowerCase() + "%"
                ));
            }
            
            // Lọc theo methodology
            if (StringUtils.hasText(filterRequest.getMethodology())) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("methodology")),
                    "%" + filterRequest.getMethodology().toLowerCase() + "%"
                ));
            }
            
            // Lọc theo supervisor ID
            if (filterRequest.getSupervisorId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("supervisorId"), filterRequest.getSupervisorId()));
            }
            
            // Lọc theo academic year ID
            if (filterRequest.getAcademicYearId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("academicYearId"), filterRequest.getAcademicYearId()));
            }
            
            // Lọc theo số lượng sinh viên tối thiểu
            if (filterRequest.getMinStudents() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                    root.get("maxStudents"), filterRequest.getMinStudents()
                ));
            }
            
            // Lọc theo số lượng sinh viên tối đa
            if (filterRequest.getMaxStudents() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                    root.get("maxStudents"), filterRequest.getMaxStudents()
                ));
            }
            
            // Lọc theo độ khó
            if (filterRequest.getDifficultyLevel() != null) {
                predicates.add(criteriaBuilder.equal(
                    root.get("difficultyLevel"), filterRequest.getDifficultyLevel()
                ));
            }
            
            // Lọc theo trạng thái đề tài
            if (filterRequest.getTopicStatus() != null) {
                predicates.add(criteriaBuilder.equal(
                    root.get("topicStatus"), filterRequest.getTopicStatus()
                ));
            }
            
            // Lọc theo trạng thái phê duyệt
            if (filterRequest.getApprovalStatus() != null) {
                predicates.add(criteriaBuilder.equal(
                    root.get("approvalStatus"), filterRequest.getApprovalStatus()
                ));
            }
            
            // Lọc theo thời gian tạo từ
            if (filterRequest.getCreatedFrom() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                    root.get("createdAt"), filterRequest.getCreatedFrom()
                ));
            }
            
            // Lọc theo thời gian tạo đến
            if (filterRequest.getCreatedTo() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                    root.get("createdAt"), filterRequest.getCreatedTo()
                ));
            }
            
            // Lọc theo thời gian cập nhật từ
            if (filterRequest.getUpdatedFrom() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                    root.get("updatedAt"), filterRequest.getUpdatedFrom()
                ));
            }
            
            // Lọc theo thời gian cập nhật đến
            if (filterRequest.getUpdatedTo() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                    root.get("updatedAt"), filterRequest.getUpdatedTo()
                ));
            }
            
            // Lọc theo người tạo
            if (filterRequest.getCreatedBy() != null) {
                predicates.add(criteriaBuilder.equal(
                    root.get("createdBy"), filterRequest.getCreatedBy()
                ));
            }
            
            // Lọc theo người cập nhật
            if (filterRequest.getUpdatedBy() != null) {
                predicates.add(criteriaBuilder.equal(
                    root.get("updatedBy"), filterRequest.getUpdatedBy()
                ));
            }
            
            // Tìm kiếm theo pattern (tìm trong title, description, objectives, methodology)
            if (StringUtils.hasText(filterRequest.getSearchPattern())) {
                String pattern = "%" + filterRequest.getSearchPattern().toLowerCase() + "%";
                Predicate titlePredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("title")), pattern
                );
                Predicate descPredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("description")), pattern
                );
                Predicate objPredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("objectives")), pattern
                );
                Predicate methodPredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("methodology")), pattern
                );
                
                predicates.add(criteriaBuilder.or(titlePredicate, descPredicate, objPredicate, methodPredicate));
            }
            
            // Lọc theo vai trò người dùng
            if (StringUtils.hasText(filterRequest.getUserRole()) && filterRequest.getUserId() != null) {
                switch (filterRequest.getUserRole().toUpperCase()) {
                    case "TEACHER":
                        // Giảng viên chỉ thấy đề tài của mình
                        predicates.add(criteriaBuilder.equal(
                            root.get("supervisorId"), filterRequest.getUserId()
                        ));
                        break;
                    case "STUDENT":
                        // Sinh viên chỉ thấy đề tài có sẵn và đã được phê duyệt
                        predicates.add(criteriaBuilder.equal(
                            root.get("topicStatus"), ProjectTopic.TopicStatus.ACTIVE
                        ));
                        predicates.add(criteriaBuilder.equal(
                            root.get("approvalStatus"), ProjectTopic.ApprovalStatus.APPROVED
                        ));
                        break;
                    case "ADMIN":
                        // Admin thấy tất cả đề tài
                        break;
                    default:
                        // Vai trò không hợp lệ, không trả về gì
                        predicates.add(criteriaBuilder.disjunction());
                        break;
                }
            }
            
            // Kết hợp tất cả predicates với AND
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
    
    /**
     * Tìm kiếm theo pattern đơn giản
     */
    public static Specification<ProjectTopic> withSearchPattern(String searchPattern) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(searchPattern)) {
                return criteriaBuilder.conjunction();
            }
            
            String pattern = "%" + searchPattern.toLowerCase() + "%";
            
            Predicate titlePredicate = criteriaBuilder.like(
                criteriaBuilder.lower(root.get("title")), pattern
            );
            Predicate descPredicate = criteriaBuilder.like(
                criteriaBuilder.lower(root.get("description")), pattern
            );
            Predicate objPredicate = criteriaBuilder.like(
                criteriaBuilder.lower(root.get("objectives")), pattern
            );
            Predicate methodPredicate = criteriaBuilder.like(
                criteriaBuilder.lower(root.get("methodology")), pattern
            );
            
            return criteriaBuilder.or(titlePredicate, descPredicate, objPredicate, methodPredicate);
        };
    }
    
    /**
     * Lọc theo giảng viên
     */
    public static Specification<ProjectTopic> withSupervisor(Integer supervisorId) {
        return (root, query, criteriaBuilder) -> {
            if (supervisorId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("supervisorId"), supervisorId);
        };
    }
    
    /**
     * Lọc theo năm học
     */
    public static Specification<ProjectTopic> withAcademicYear(Integer academicYearId) {
        return (root, query, criteriaBuilder) -> {
            if (academicYearId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("academicYearId"), academicYearId);
        };
    }
    
    /**
     * Lọc theo độ khó
     */
    public static Specification<ProjectTopic> withDifficultyLevel(ProjectTopic.DifficultyLevel difficultyLevel) {
        return (root, query, criteriaBuilder) -> {
            if (difficultyLevel == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("difficultyLevel"), difficultyLevel);
        };
    }
    
    /**
     * Lọc theo trạng thái đề tài
     */
    public static Specification<ProjectTopic> withTopicStatus(ProjectTopic.TopicStatus topicStatus) {
        return (root, query, criteriaBuilder) -> {
            if (topicStatus == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("topicStatus"), topicStatus);
        };
    }
    
    /**
     * Lọc theo trạng thái phê duyệt
     */
    public static Specification<ProjectTopic> withApprovalStatus(ProjectTopic.ApprovalStatus approvalStatus) {
        return (root, query, criteriaBuilder) -> {
            if (approvalStatus == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("approvalStatus"), approvalStatus);
        };
    }
    
    /**
     * Lọc theo số lượng sinh viên
     */
    public static Specification<ProjectTopic> withStudentRange(Integer minStudents, Integer maxStudents) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (minStudents != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                    root.get("maxStudents"), minStudents
                ));
            }
            
            if (maxStudents != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                    root.get("maxStudents"), maxStudents
                ));
            }
            
            if (predicates.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
    
    /**
     * Lọc theo thời gian tạo
     */
    public static Specification<ProjectTopic> withCreatedTimeRange(Instant from, Instant to) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (from != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                    root.get("createdAt"), from
                ));
            }
            
            if (to != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                    root.get("createdAt"), to
                ));
            }
            
            if (predicates.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
    
    /**
     * Lọc theo thời gian cập nhật
     */
    public static Specification<ProjectTopic> withUpdatedTimeRange(Instant from, Instant to) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (from != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                    root.get("updatedAt"), from
                ));
            }
            
            if (to != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                    root.get("updatedAt"), to
                ));
            }
            
            if (predicates.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
