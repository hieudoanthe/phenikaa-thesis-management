package com.phenikaa.userservice.dto.request;

import lombok.Data;
import java.util.List;

@Data
public class DynamicFilterRequest {
    
    private List<FilterCriteria> criteria;
    private List<SortCriteria> sortCriteria;
    private PaginationRequest pagination;
    
    @Data
    public static class FilterCriteria {
        private String field;           // Tên trường (username, fullName, status, etc.)
        private String operator;        // Toán tử (eq, ne, gt, lt, gte, lte, like, in, between)
        private Object value;           // Giá trị đơn
        private Object value2;          // Giá trị thứ 2 (cho between)
        private List<Object> values;    // Danh sách giá trị (cho in)
        private String logicalOperator; // AND, OR (mặc định AND)
    }
    
    @Data
    public static class SortCriteria {
        private String field;           // Tên trường sắp xếp
        private String direction;       // ASC, DESC
    }
    
    @Data
    public static class PaginationRequest {
        private Integer page = 0;
        private Integer size = 10;
    }
    
    // Các operator được hỗ trợ
    public static class Operators {
        public static final String EQUALS = "eq";
        public static final String NOT_EQUALS = "ne";
        public static final String GREATER_THAN = "gt";
        public static final String LESS_THAN = "lt";
        public static final String GREATER_EQUALS = "gte";
        public static final String LESS_EQUALS = "lte";
        public static final String LIKE = "like";
        public static final String IN = "in";
        public static final String NOT_IN = "nin";
        public static final String BETWEEN = "between";
        public static final String IS_NULL = "isNull";
        public static final String IS_NOT_NULL = "isNotNull";
        public static final String STARTS_WITH = "startsWith";
        public static final String ENDS_WITH = "endsWith";
        public static final String CONTAINS = "contains";
    }
    
    // Các logical operator
    public static class LogicalOperators {
        public static final String AND = "AND";
        public static final String OR = "OR";
    }
}
