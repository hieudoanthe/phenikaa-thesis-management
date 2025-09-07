package com.phenikaa.evalservice.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommitteeEvaluationPDFRequest {
    
    // Thông tin sinh viên và đề tài
    private Integer topicId;
    private Integer studentId;
    private String studentName;
    private String studentIdNumber;
    private String className;
    private String major;
    private String batch;
    private String topicTitle;
    private String supervisorName;
    private String supervisorTitle;
    
    // Thông tin hội đồng đánh giá
    private String evaluatorName;
    private String evaluatorTitle;
    private String evaluatorDepartment;
    private LocalDateTime evaluationDate;
    
    // Điểm số từng tiêu chí (theo format phiếu đánh giá hội đồng)
    private Float presentationClarityScore; // Trình bày nội dung (0-0.5)
    private Float reviewerQaScore; // Trả lời câu hỏi GVPB (0-1.5)
    private Float committeeQaScore; // Trả lời câu hỏi hội đồng (0-1.5)
    private Float attitudeScore; // Tinh thần, thái độ và cách ứng xử (0-1.0)
    private Float contentImplementationScore; // Thực hiện nội dung đề tài (0-4.5)
    private Float relatedIssuesScore; // Mối liên hệ với vấn đề liên quan (0-1.0)
    
    private Float totalScore; // Tổng điểm (0-10)
    private String comments; // Nhận xét
    
    // Q&A data
    private List<QnAData> qnaData;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QnAData {
        private String question;
        private String answer;
        private String questionerName;
        private LocalDateTime questionTime;
        private LocalDateTime answerTime;
    }
}
