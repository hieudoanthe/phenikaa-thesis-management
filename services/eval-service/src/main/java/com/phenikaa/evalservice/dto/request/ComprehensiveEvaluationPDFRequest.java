package com.phenikaa.evalservice.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComprehensiveEvaluationPDFRequest {
    
    // Thông tin sinh viên và đề tài
    private Integer topicId;
    private String studentName;
    private String studentIdNumber;
    private String className;
    private String major;
    private String batch;
    private String topicTitle;
    private LocalDateTime evaluationDate;
    private String room;
    
    // Thông tin hội đồng đánh giá
    private CommitteeMember chairman;
    private CommitteeMember secretary;
    private CommitteeMember member;
    private Reviewer reviewer;
    private Supervisor supervisor;
    
    // Q&A data
    private List<QnAData> qnaData;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CommitteeMember {
        private String name;
        private String title;
        private String department;
        
        // Điểm số theo format phiếu đánh giá hội đồng
        private Float presentationClarityScore; // Trình bày nội dung (0-0.5)
        private Float reviewerQaScore; // Trả lời câu hỏi GVPB (0-1.5)
        private Float committeeQaScore; // Trả lời câu hỏi hội đồng (0-1.5)
        private Float attitudeScore; // Tinh thần, thái độ và cách ứng xử (0-1.0)
        private Float contentImplementationScore; // Thực hiện nội dung đề tài (0-4.5)
        private Float relatedIssuesScore; // Mối liên hệ với vấn đề liên quan (0-1.0)
        private Float totalScore; // Tổng điểm (0-10)
        private String comments;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Reviewer {
        private String name;
        private String title;
        private String department;
        
        // Điểm số theo format phiếu đánh giá phản biện
        private Float presentationFormatScore; // Hình thức trình bày (0-1.5)
        private Float contentImplementationScore; // Thực hiện nội dung đề tài (0-4.0)
        private Float relatedIssuesScore; // Mối liên hệ với vấn đề liên quan (0-2.0)
        private Float practicalApplicationScore; // Tính ứng dụng thực tiễn (0-2.0)
        private Float bonusScore; // Điểm thưởng (0-0.5)
        private Float totalScore; // Tổng điểm (0-10)
        private String comments;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Supervisor {
        private String name;
        private String title;
        private String department;
        
        // Điểm số theo format phiếu đánh giá hướng dẫn
        private Float attitudeScore; // Ý thức và thái độ (0-1.0)
        private Float problemSolvingScore; // Khả năng xử lý vấn đề (0-1.0)
        private Float presentationFormatScore; // Hình thức trình bày (0-1.5)
        private Float contentImplementationScore; // Thực hiện nội dung đề tài (0-4.5)
        private Float relatedIssuesScore; // Mối liên hệ với vấn đề liên quan (0-1.0)
        private Float practicalApplicationScore; // Tính ứng dụng thực tiễn (0-1.0)
        private Float totalScore; // Tổng điểm (0-10)
        private String comments;
    }
    
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
