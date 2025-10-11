package com.phenikaa.userservice.filter;

import com.phenikaa.userservice.dto.request.DynamicFilterRequest;
import com.phenikaa.userservice.entity.User;
import com.phenikaa.userservice.entity.Role;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import jakarta.persistence.criteria.*;
import java.util.List;

public class DynamicFilterBuilder {

    public static Specification<User> buildSpecification(DynamicFilterRequest request) {
        return (root, query, criteriaBuilder) -> {
            if (request.getCriteria() == null || request.getCriteria().isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            
            List<Predicate> predicates = new java.util.ArrayList<>();
            
            for (DynamicFilterRequest.FilterCriteria criteria : request.getCriteria()) {
                Predicate predicate = buildPredicate(root, criteriaBuilder, criteria);
                if (predicate != null) {
                    predicates.add(predicate);
                }
            }
            
            // Kết hợp tất cả predicates
            if (predicates.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            
            // Xử lý logical operator
            return combinePredicates(predicates, criteriaBuilder, request.getCriteria());
        };
    }
    
    /**
     * Xây dựng predicate cho từng criteria
     */
    private static Predicate buildPredicate(Root<User> root, CriteriaBuilder criteriaBuilder, 
                                          DynamicFilterRequest.FilterCriteria criteria) {
        
        if (!StringUtils.hasText(criteria.getField()) || !StringUtils.hasText(criteria.getOperator())) {
            return null;
        }
        
        String field = criteria.getField();
        String operator = criteria.getOperator();
        
        try {
            switch (operator) {
                case DynamicFilterRequest.Operators.EQUALS:
                    return criteriaBuilder.equal(root.get(field), criteria.getValue());
                    
                case DynamicFilterRequest.Operators.NOT_EQUALS:
                    return criteriaBuilder.notEqual(root.get(field), criteria.getValue());
                    
                case DynamicFilterRequest.Operators.GREATER_THAN:
                    return criteriaBuilder.greaterThan(root.get(field), (Comparable) criteria.getValue());
                    
                case DynamicFilterRequest.Operators.LESS_THAN:
                    return criteriaBuilder.lessThan(root.get(field), (Comparable) criteria.getValue());
                    
                case DynamicFilterRequest.Operators.GREATER_EQUALS:
                    return criteriaBuilder.greaterThanOrEqualTo(root.get(field), (Comparable) criteria.getValue());
                    
                case DynamicFilterRequest.Operators.LESS_EQUALS:
                    return criteriaBuilder.lessThanOrEqualTo(root.get(field), (Comparable) criteria.getValue());
                    
                case DynamicFilterRequest.Operators.LIKE:
                    return criteriaBuilder.like(
                        criteriaBuilder.lower(root.get(field)),
                        "%" + criteria.getValue().toString().toLowerCase() + "%"
                    );
                    
                case DynamicFilterRequest.Operators.STARTS_WITH:
                    return criteriaBuilder.like(
                        criteriaBuilder.lower(root.get(field)),
                        criteria.getValue().toString().toLowerCase() + "%"
                    );
                    
                case DynamicFilterRequest.Operators.ENDS_WITH:
                    return criteriaBuilder.like(
                        criteriaBuilder.lower(root.get(field)),
                        "%" + criteria.getValue().toString().toLowerCase()
                    );
                    
                case DynamicFilterRequest.Operators.CONTAINS:
                    return criteriaBuilder.like(
                        criteriaBuilder.lower(root.get(field)),
                        "%" + criteria.getValue().toString().toLowerCase() + "%"
                    );
                    
                case DynamicFilterRequest.Operators.IN:
                    if (criteria.getValues() != null && !criteria.getValues().isEmpty()) {
                        return root.get(field).in(criteria.getValues());
                    }
                    return null;
                    
                case DynamicFilterRequest.Operators.NOT_IN:
                    if (criteria.getValues() != null && !criteria.getValues().isEmpty()) {
                        return criteriaBuilder.not(root.get(field).in(criteria.getValues()));
                    }
                    return null;
                    
                case DynamicFilterRequest.Operators.BETWEEN:
                    if (criteria.getValue() != null && criteria.getValue2() != null) {
                        return criteriaBuilder.between(
                            root.get(field), 
                            (Comparable) criteria.getValue(), 
                            (Comparable) criteria.getValue2()
                        );
                    }
                    return null;
                    
                case DynamicFilterRequest.Operators.IS_NULL:
                    return criteriaBuilder.isNull(root.get(field));
                    
                case DynamicFilterRequest.Operators.IS_NOT_NULL:
                    return criteriaBuilder.isNotNull(root.get(field));
                    
                // Xử lý các trường đặc biệt
                default:
                    return handleSpecialFields(root, criteriaBuilder, criteria);
            }
        } catch (Exception e) {
            // Log error và trả về null để bỏ qua criteria này
            System.err.println("Error building predicate for field: " + field + ", operator: " + operator + ", error: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Xử lý các trường đặc biệt như roles, relationships
     */
    private static Predicate handleSpecialFields(Root<User> root, CriteriaBuilder criteriaBuilder, 
                                               DynamicFilterRequest.FilterCriteria criteria) {
        
        String field = criteria.getField();
        String operator = criteria.getOperator();
        
        // Xử lý trường roles
        if ("roles".equals(field)) {
            if (DynamicFilterRequest.Operators.IN.equals(operator) && criteria.getValues() != null) {
                Join<User, Role> roleJoin = root.join("roles", JoinType.INNER);
                return roleJoin.get("roleName").in(criteria.getValues());
            }
        }
        
        // Xử lý trường createdBy (thời gian tạo)
        if ("createdBy".equals(field)) {
            if (DynamicFilterRequest.Operators.BETWEEN.equals(operator) && 
                criteria.getValue() != null && criteria.getValue2() != null) {
                return criteriaBuilder.between(
                    root.get("createdBy"), 
                    (Comparable) criteria.getValue(), 
                    (Comparable) criteria.getValue2()
                );
            }
        }
        
        // Xử lý trường lastLogin
        if ("lastLogin".equals(field)) {
            if (DynamicFilterRequest.Operators.BETWEEN.equals(operator) && 
                criteria.getValue() != null && criteria.getValue2() != null) {
                return criteriaBuilder.between(
                    root.get("lastLogin"), 
                    (Comparable) criteria.getValue(), 
                    (Comparable) criteria.getValue2()
                );
            }
        }
        
        return null;
    }
    
    /**
     * Kết hợp các predicates theo logical operator
     */
    private static Predicate combinePredicates(List<Predicate> predicates, CriteriaBuilder criteriaBuilder,
                                             List<DynamicFilterRequest.FilterCriteria> criteriaList) {
        
        if (predicates.size() == 1) {
            return predicates.get(0);
        }
        
        // Kiểm tra xem có sử dụng OR không
        boolean hasOR = criteriaList.stream()
            .anyMatch(c -> DynamicFilterRequest.LogicalOperators.OR.equals(c.getLogicalOperator()));
        
        if (hasOR) {
            // Xử lý phức tạp hơn với OR
            return buildComplexLogicalPredicate(predicates, criteriaBuilder, criteriaList);
        } else {
            // Mặc định sử dụng AND
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        }
    }
    
    /**
     * Xây dựng predicate phức tạp với OR
     */
    private static Predicate buildComplexLogicalPredicate(List<Predicate> predicates, CriteriaBuilder criteriaBuilder,
                                                        List<DynamicFilterRequest.FilterCriteria> criteriaList) {
        
        List<Predicate> andPredicates = new java.util.ArrayList<>();
        List<Predicate> orPredicates = new java.util.ArrayList<>();
        
        for (int i = 0; i < predicates.size(); i++) {
            DynamicFilterRequest.FilterCriteria criteria = criteriaList.get(i);
            if (DynamicFilterRequest.LogicalOperators.OR.equals(criteria.getLogicalOperator())) {
                orPredicates.add(predicates.get(i));
            } else {
                andPredicates.add(predicates.get(i));
            }
        }
        
        Predicate finalPredicate = null;
        
        // Xử lý AND predicates
        if (!andPredicates.isEmpty()) {
            finalPredicate = criteriaBuilder.and(andPredicates.toArray(new Predicate[0]));
        }
        
        // Xử lý OR predicates
        if (!orPredicates.isEmpty()) {
            Predicate orPredicate = criteriaBuilder.or(orPredicates.toArray(new Predicate[0]));
            if (finalPredicate != null) {
                finalPredicate = criteriaBuilder.and(finalPredicate, orPredicate);
            } else {
                finalPredicate = orPredicate;
            }
        }
        
        return finalPredicate != null ? finalPredicate : criteriaBuilder.conjunction();
    }
}
