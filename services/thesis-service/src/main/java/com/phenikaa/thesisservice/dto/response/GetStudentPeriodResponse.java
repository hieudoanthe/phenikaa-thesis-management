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
    private Integer studentId; // ID của sinh viên (suggestedBy hoặc studentId từ Register)
    private Integer supervisorId; // ID của giảng viên hướng dẫn
    private SuggestedTopic.SuggestionStatus suggestionStatus; // Trạng thái đề xuất (nếu có)
    private String registrationType; // "REGISTERED" hoặc "SUGGESTED"
    private Integer topicId; // ID của đề tài
    private String topicTitle; // Tiêu đề đề tài
    private String topicCode; // Mã đề tài
}
