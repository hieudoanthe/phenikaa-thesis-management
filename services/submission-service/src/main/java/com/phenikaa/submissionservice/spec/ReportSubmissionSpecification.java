package com.phenikaa.submissionservice.spec;

import com.phenikaa.submissionservice.entity.ReportSubmission;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class ReportSubmissionSpecification {

    private ReportSubmissionSpecification() {
        // Private constructor to prevent instantiation
    }

    public static Specification<ReportSubmission> withFilter(String search, Integer submissionType, Integer submittedBy) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (submittedBy != null) {
                predicates.add(cb.equal(root.get("submittedBy"), submittedBy));
            }

            if (search != null && !search.isBlank()) {
                String like = "%" + search.trim().toLowerCase() + "%";
                // For now, only search in reportTitle to avoid TEXT conversion issues
                // TODO: Implement proper TEXT field search using @Query annotation
                Predicate byTitle = cb.like(cb.lower(root.get("reportTitle")), like);
                predicates.add(byTitle);
            }

            if (submissionType != null) {
                predicates.add(cb.equal(root.get("submissionType"), submissionType));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}


