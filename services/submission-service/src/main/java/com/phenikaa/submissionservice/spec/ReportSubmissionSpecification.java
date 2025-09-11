package com.phenikaa.submissionservice.spec;

import com.phenikaa.submissionservice.entity.ReportSubmission;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class ReportSubmissionSpecification {

    public static Specification<ReportSubmission> withFilter(String search, Integer submissionType, Integer submittedBy) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (submittedBy != null) {
                predicates.add(cb.equal(root.get("submittedBy"), submittedBy));
            }

            if (search != null && !search.isBlank()) {
                String like = "%" + search.trim().toLowerCase() + "%";
                Predicate byTitle = cb.like(cb.lower(root.get("reportTitle")), like);
                Predicate byDesc  = cb.like(cb.lower(root.get("description")), like);
                predicates.add(cb.or(byTitle, byDesc));
            }

            if (submissionType != null) {
                predicates.add(cb.equal(root.get("submissionType"), submissionType));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}


