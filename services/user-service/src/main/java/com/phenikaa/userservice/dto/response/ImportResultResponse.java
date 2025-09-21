package com.phenikaa.userservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImportResultResponse {
    private boolean success;
    private String message;
    private int totalRows;
    private int successCount;
    private int errorCount;
    private List<ImportError> errors;
    private List<StudentImportResult> results;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImportError {
        private int rowNumber;
        private String field;
        private String message;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentImportResult {
        private String studentId;
        private String fullName;
        private String username;
        private boolean success;
        private String message;
        private Integer userId;
    }
}
