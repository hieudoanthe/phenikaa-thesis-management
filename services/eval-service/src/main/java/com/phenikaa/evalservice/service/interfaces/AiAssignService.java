package com.phenikaa.evalservice.service.interfaces;

public interface AiAssignService {
    /**
     * Generate AI-based preview schedule from prepared payload.
     * The payload should contain keys: "students", "sessions", "sessionReviewers", "reviewerSpecialization".
     */
    com.phenikaa.evalservice.dto.AutoAssignPreviewResponse generatePreview(java.util.Map<String, Object> payload);
}
