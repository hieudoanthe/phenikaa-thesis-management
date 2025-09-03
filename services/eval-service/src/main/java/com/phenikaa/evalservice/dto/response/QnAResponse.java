package com.phenikaa.evalservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QnAResponse {
    
    private Integer qnaId;
    private Integer topicId;
    private Integer studentId;
    private Integer questionerId;
    private Integer secretaryId;
    private String question;
    private String answer;
    private LocalDateTime questionTime;
    private LocalDateTime answerTime;
    
    // Thông tin bổ sung
    private String studentName;
    private String topicTitle;
    private String questionerName;
    private String secretaryName;
}
