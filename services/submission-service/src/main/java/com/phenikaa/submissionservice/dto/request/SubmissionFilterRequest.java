package com.phenikaa.submissionservice.dto.request;

import lombok.Data;

@Data
public class SubmissionFilterRequest {
    private String search;
    private Integer submissionType;
    private Integer page = 0;
    private Integer size = 10;
}


