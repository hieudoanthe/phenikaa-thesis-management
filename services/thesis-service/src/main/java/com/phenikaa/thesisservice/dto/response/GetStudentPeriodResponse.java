package com.phenikaa.thesisservice.dto.response;

import com.phenikaa.thesisservice.entity.SuggestedTopic;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetStudentPeriodResponse {
    private Integer registrationPeriodId;
    private Integer studentId;
    private String username;
    private String fullName;
    private Integer supervisorId;
    private String supervisorFullName;
    private SuggestedTopic.SuggestionStatus suggestionStatus;
    private String registrationType;
    private Integer topicId;
    private String topicTitle;
    private String topicCode;
}
