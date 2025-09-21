package com.phenikaa.userservice.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImportStudentsRequest {
    private Integer periodId;
    private Integer academicYearId;
    private List<StudentImportData> students;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentImportData {
        private String fullName;
        private String email;
        private String password;
    }
}
