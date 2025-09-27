package com.phenikaa.thesisservice.dto.response;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class StudentsByPeriodResponse {
    private boolean success;
    private List<Map<String, Object>> data;
    private String message;
}
