package com.phenikaa.evalservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FinalScoreResponse {
    
    private Integer topicId;
    private Integer studentId;
    private String studentName;
    private String topicTitle;
    
    // Điểm từng loại đánh giá
    private Float supervisorScore;    // Điểm GVHD (25%)
    private Float reviewerScore;      // Điểm GVPB (50%)
    private Float committeeScore;     // Điểm HĐ (25%)
    
    // Điểm cuối cùng
    private Float finalScore;
    
    // Chi tiết đánh giá
    private List<EvaluationResponse> evaluations;
    
    // Trạng thái
    private String status; // COMPLETED, INCOMPLETE, PENDING
}
