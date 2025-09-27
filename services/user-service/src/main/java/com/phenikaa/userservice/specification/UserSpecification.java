package com.phenikaa.userservice.specification;

import com.phenikaa.userservice.dto.request.UserFilterRequest;
import com.phenikaa.userservice.entity.User;
import com.phenikaa.userservice.entity.Role;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import jakarta.persistence.criteria.*;
import java.time.LocalDateTime;
import java.util.List;

public class UserSpecification {
    
    /**
     * Tạo specification để filter người dùng theo các tiêu chí
     */
    public static Specification<User> withFilter(UserFilterRequest filterRequest) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new java.util.ArrayList<>();
            
            // Filter theo username
            if (StringUtils.hasText(filterRequest.getUsername())) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("username")),
                    "%" + filterRequest.getUsername().toLowerCase() + "%"
                ));
            }
            
            // Filter theo fullName
            if (StringUtils.hasText(filterRequest.getFullName())) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("fullName")),
                    "%" + filterRequest.getFullName().toLowerCase() + "%"
                ));
            }
            
            // Filter theo status
            if (filterRequest.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), filterRequest.getStatus()));
            }
            
            // Filter theo vai trò
            if (filterRequest.getRoleNames() != null && !filterRequest.getRoleNames().isEmpty()) {
                Join<User, Role> roleJoin = root.join("roles", JoinType.INNER);
                predicates.add(roleJoin.get("roleName").in(
                    filterRequest.getRoleNames().stream()
                        .map(roleName -> Role.RoleName.valueOf(roleName))
                        .toList()
                ));
            }
            
            // Filter theo thời gian tạo
            if (filterRequest.getCreatedFrom() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                    root.get("createdBy"), filterRequest.getCreatedFrom()
                ));
            }
            
            if (filterRequest.getCreatedTo() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                    root.get("createdBy"), filterRequest.getCreatedTo()
                ));
            }
            
            // Filter theo thời gian đăng nhập cuối
            if (filterRequest.getLastLoginFrom() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                    root.get("lastLogin"), filterRequest.getLastLoginFrom()
                ));
            }
            
            if (filterRequest.getLastLoginTo() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                    root.get("lastLogin"), filterRequest.getLastLoginTo()
                ));
            }
            
            // Tìm kiếm theo pattern (trong username và fullName)
            if (StringUtils.hasText(filterRequest.getSearchPattern())) {
                String pattern = "%" + filterRequest.getSearchPattern().toLowerCase() + "%";
                Predicate usernamePredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("username")), pattern
                );
                Predicate fullNamePredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("fullName")), pattern
                );
                predicates.add(criteriaBuilder.or(usernamePredicate, fullNamePredicate));
            }
            
            // Kết hợp tất cả predicates với AND
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
    
    /**
     * Tạo specification để tìm kiếm người dùng theo pattern
     */
    public static Specification<User> withSearchPattern(String searchPattern) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(searchPattern)) {
                return criteriaBuilder.conjunction();
            }
            
            String pattern = "%" + searchPattern.toLowerCase() + "%";
            Predicate usernamePredicate = criteriaBuilder.like(
                criteriaBuilder.lower(root.get("username")), pattern
            );
            Predicate fullNamePredicate = criteriaBuilder.like(
                criteriaBuilder.lower(root.get("fullName")), pattern
            );
            
            return criteriaBuilder.or(usernamePredicate, fullNamePredicate);
        };
    }
    
    /**
     * Tạo specification để filter theo vai trò
     */
    public static Specification<User> withRole(Role.RoleName roleName) {
        return (root, query, criteriaBuilder) -> {
            Join<User, Role> roleJoin = root.join("roles", JoinType.INNER);
            return criteriaBuilder.equal(roleJoin.get("roleName"), roleName);
        };
    }
    
    /**
     * Tạo specification để filter theo trạng thái
     */
    public static Specification<User> withStatus(Integer status) {
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.equal(root.get("status"), status);
    }
    
    /**
     * Tạo specification để filter theo thời gian đăng nhập cuối
     */
    public static Specification<User> withLastLoginBetween(LocalDateTime start, LocalDateTime end) {
        return (root, query, criteriaBuilder) -> {
            Predicate startPredicate = criteriaBuilder.greaterThanOrEqualTo(root.get("lastLogin"), start);
            Predicate endPredicate = criteriaBuilder.lessThanOrEqualTo(root.get("lastLogin"), end);
            Predicate notNullPredicate = criteriaBuilder.isNotNull(root.get("lastLogin"));
            
            return criteriaBuilder.and(startPredicate, endPredicate, notNullPredicate);
        };
    }
    
    /**
     * Tạo specification để filter theo period ID
     */
    public static Specification<User> withPeriodId(Integer periodId) {
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.equal(root.get("periodId"), periodId);
    }
}
