package com.phenikaa.userservice.filter;

import com.phenikaa.userservice.dto.request.DynamicFilterRequest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;

public class DynamicQueryBuilder {
    
    /**
     * Tạo Pageable từ dynamic filter request
     */
    public static Pageable buildPageable(DynamicFilterRequest request) {
        if (request.getPagination() == null) {
            return PageRequest.of(0, 10);
        }
        
        int page = request.getPagination().getPage() != null ? request.getPagination().getPage() : 0;
        int size = request.getPagination().getSize() != null ? request.getPagination().getSize() : 10;
        
        // Tạo Sort từ sort criteria
        Sort sort = buildSort(request.getSortCriteria());
        
        return PageRequest.of(page, size, sort);
    }
    
    /**
     * Tạo Sort từ sort criteria
     */
    public static Sort buildSort(List<DynamicFilterRequest.SortCriteria> sortCriteria) {
        if (sortCriteria == null || sortCriteria.isEmpty()) {
            return Sort.by("userId").ascending(); // Default sort
        }
        
        List<Sort.Order> orders = new ArrayList<>();
        
        for (DynamicFilterRequest.SortCriteria criteria : sortCriteria) {
            if (criteria.getField() != null && criteria.getDirection() != null) {
                Sort.Direction direction = "DESC".equalsIgnoreCase(criteria.getDirection()) ? 
                    Sort.Direction.DESC : Sort.Direction.ASC;
                
                orders.add(new Sort.Order(direction, criteria.getField()));
            }
        }
        
        if (orders.isEmpty()) {
            return Sort.by("userId").ascending(); // Default sort
        }
        
        return Sort.by(orders);
    }
    
    /**
     * Validate dynamic filter request
     */
    public static List<String> validateRequest(DynamicFilterRequest request) {
        List<String> errors = new ArrayList<>();
        
        if (request == null) {
            errors.add("Request không được null");
            return errors;
        }
        
        // Validate pagination
        if (request.getPagination() != null) {
            if (request.getPagination().getPage() != null && request.getPagination().getPage() < 0) {
                errors.add("Page phải >= 0");
            }
            if (request.getPagination().getSize() != null && request.getPagination().getSize() <= 0) {
                errors.add("Size phải > 0");
            }
            if (request.getPagination().getSize() != null && request.getPagination().getSize() > 1000) {
                errors.add("Size không được > 1000");
            }
        }
        
        // Validate criteria
        if (request.getCriteria() != null) {
            for (int i = 0; i < request.getCriteria().size(); i++) {
                DynamicFilterRequest.FilterCriteria criteria = request.getCriteria().get(i);
                List<String> criteriaErrors = validateCriteria(criteria, i);
                errors.addAll(criteriaErrors);
            }
        }
        
        // Validate sort criteria
        if (request.getSortCriteria() != null) {
            for (int i = 0; i < request.getSortCriteria().size(); i++) {
                DynamicFilterRequest.SortCriteria criteria = request.getSortCriteria().get(i);
                List<String> sortErrors = validateSortCriteria(criteria, i);
                errors.addAll(sortErrors);
            }
        }
        
        return errors;
    }
    
    /**
     * Validate filter criteria
     */
    private static List<String> validateCriteria(DynamicFilterRequest.FilterCriteria criteria, int index) {
        List<String> errors = new ArrayList<>();
        
        if (criteria.getField() == null || criteria.getField().trim().isEmpty()) {
            errors.add("Criteria[" + index + "]: Field không được để trống");
        }
        
        if (criteria.getOperator() == null || criteria.getOperator().trim().isEmpty()) {
            errors.add("Criteria[" + index + "]: Operator không được để trống");
        } else {
            // Validate operator
            if (!isValidOperator(criteria.getOperator())) {
                errors.add("Criteria[" + index + "]: Operator '" + criteria.getOperator() + "' không hợp lệ");
            }
        }
        
        // Validate value based on operator
        if (criteria.getOperator() != null) {
            switch (criteria.getOperator()) {
                case DynamicFilterRequest.Operators.IS_NULL:
                case DynamicFilterRequest.Operators.IS_NOT_NULL:
                    // Không cần value
                    break;
                    
                case DynamicFilterRequest.Operators.BETWEEN:
                    if (criteria.getValue() == null || criteria.getValue2() == null) {
                        errors.add("Criteria[" + index + "]: BETWEEN operator cần cả value và value2");
                    }
                    break;
                    
                case DynamicFilterRequest.Operators.IN:
                case DynamicFilterRequest.Operators.NOT_IN:
                    if (criteria.getValues() == null || criteria.getValues().isEmpty()) {
                        errors.add("Criteria[" + index + "]: IN/NOT_IN operator cần values array");
                    }
                    break;
                    
                default:
                    if (criteria.getValue() == null) {
                        errors.add("Criteria[" + index + "]: Operator '" + criteria.getOperator() + "' cần value");
                    }
                    break;
            }
        }
        
        // Validate logical operator
        if (criteria.getLogicalOperator() != null) {
            if (!DynamicFilterRequest.LogicalOperators.AND.equals(criteria.getLogicalOperator()) &&
                !DynamicFilterRequest.LogicalOperators.OR.equals(criteria.getLogicalOperator())) {
                errors.add("Criteria[" + index + "]: Logical operator phải là 'AND' hoặc 'OR'");
            }
        }
        
        return errors;
    }
    
    /**
     * Validate sort criteria
     */
    private static List<String> validateSortCriteria(DynamicFilterRequest.SortCriteria criteria, int index) {
        List<String> errors = new ArrayList<>();
        
        if (criteria.getField() == null || criteria.getField().trim().isEmpty()) {
            errors.add("SortCriteria[" + index + "]: Field không được để trống");
        }
        
        if (criteria.getDirection() != null) {
            if (!"ASC".equalsIgnoreCase(criteria.getDirection()) && 
                !"DESC".equalsIgnoreCase(criteria.getDirection())) {
                errors.add("SortCriteria[" + index + "]: Direction phải là 'ASC' hoặc 'DESC'");
            }
        }
        
        return errors;
    }
    
    /**
     * Kiểm tra operator có hợp lệ không
     */
    private static boolean isValidOperator(String operator) {
        return DynamicFilterRequest.Operators.EQUALS.equals(operator) ||
               DynamicFilterRequest.Operators.NOT_EQUALS.equals(operator) ||
               DynamicFilterRequest.Operators.GREATER_THAN.equals(operator) ||
               DynamicFilterRequest.Operators.LESS_THAN.equals(operator) ||
               DynamicFilterRequest.Operators.GREATER_EQUALS.equals(operator) ||
               DynamicFilterRequest.Operators.LESS_EQUALS.equals(operator) ||
               DynamicFilterRequest.Operators.LIKE.equals(operator) ||
               DynamicFilterRequest.Operators.IN.equals(operator) ||
               DynamicFilterRequest.Operators.NOT_IN.equals(operator) ||
               DynamicFilterRequest.Operators.BETWEEN.equals(operator) ||
               DynamicFilterRequest.Operators.IS_NULL.equals(operator) ||
               DynamicFilterRequest.Operators.IS_NOT_NULL.equals(operator) ||
               DynamicFilterRequest.Operators.STARTS_WITH.equals(operator) ||
               DynamicFilterRequest.Operators.ENDS_WITH.equals(operator) ||
               DynamicFilterRequest.Operators.CONTAINS.equals(operator);
    }
}
