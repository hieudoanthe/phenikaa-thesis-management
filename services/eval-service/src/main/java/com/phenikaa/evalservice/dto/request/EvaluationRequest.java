package com.phenikaa.evalservice.dto.request;

import com.phenikaa.evalservice.entity.ProjectEvaluation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EvaluationRequest {
    
    @NotNull(message = "Topic ID không được để trống")
    private Integer topicId;

    @NotNull(message = "Student ID không được để trống")
    private Integer studentId;

    @NotNull(message = "Evaluator ID không được để trống")
    private Integer evaluatorId;

    @NotNull(message = "Loại đánh giá không được để trống")
    private ProjectEvaluation.EvaluationType evaluationType;

    // Các trường điểm cũ (giữ lại để tương thích - không validation)
    private Float contentScore;
    private Float presentationScore;
    private Float technicalScore;
    private Float innovationScore;
    private Float defenseScore;

    // Các trường điểm mới cho từng vai trò
    // Hội đồng (COMMITTEE) - 6 tiêu chí
    @DecimalMin(value = "0.0", message = "Điểm trình bày nội dung phải >= 0")
    @DecimalMax(value = "0.5", message = "Điểm trình bày nội dung phải <= 0.5")
    private Float presentationClarityScore; // Trình bày nội dung (0-0.5)

    @DecimalMin(value = "0.0", message = "Điểm trả lời câu hỏi GVPB phải >= 0")
    @DecimalMax(value = "1.5", message = "Điểm trả lời câu hỏi GVPB phải <= 1.5")
    private Float reviewerQaScore; // Trả lời câu hỏi GVPB (0-1.5)

    @DecimalMin(value = "0.0", message = "Điểm trả lời câu hỏi hội đồng phải >= 0")
    @DecimalMax(value = "1.5", message = "Điểm trả lời câu hỏi hội đồng phải <= 1.5")
    private Float committeeQaScore; // Trả lời câu hỏi hội đồng (0-1.5)

    @DecimalMin(value = "0.0", message = "Điểm tinh thần, thái độ phải >= 0")
    @DecimalMax(value = "1.0", message = "Điểm tinh thần, thái độ phải <= 1.0")
    private Float attitudeScore; // Tinh thần, thái độ (0-1)

    @DecimalMin(value = "0.0", message = "Điểm thực hiện nội dung đề tài phải >= 0")
    @DecimalMax(value = "4.5", message = "Điểm thực hiện nội dung đề tài phải <= 4.5")
    private Float contentImplementationScore; // Thực hiện nội dung đề tài (0-4.5)

    @DecimalMin(value = "0.0", message = "Điểm mối liên hệ vấn đề liên quan phải >= 0")
    @DecimalMax(value = "1.0", message = "Điểm mối liên hệ vấn đề liên quan phải <= 1.0")
    private Float relatedIssuesScore; // Mối liên hệ vấn đề liên quan (0-1)

    // Giảng viên phản biện (REVIEWER) - 5 tiêu chí
    @DecimalMin(value = "0.0", message = "Điểm hình thức trình bày phải >= 0")
    @DecimalMax(value = "1.5", message = "Điểm hình thức trình bày phải <= 1.5")
    private Float formatScore; // Hình thức trình bày (0-1.5)

    @DecimalMin(value = "0.0", message = "Điểm thực hiện nội dung đề tài phải >= 0")
    @DecimalMax(value = "4.0", message = "Điểm thực hiện nội dung đề tài phải <= 4.0")
    private Float contentQualityScore; // Thực hiện nội dung đề tài (0-4)

    @DecimalMin(value = "0.0", message = "Điểm mối liên hệ vấn đề liên quan phải >= 0")
    @DecimalMax(value = "2.0", message = "Điểm mối liên hệ vấn đề liên quan phải <= 2.0")
    private Float relatedIssuesReviewerScore; // Mối liên hệ vấn đề liên quan (0-2)

    @DecimalMin(value = "0.0", message = "Điểm tính ứng dụng thực tiễn phải >= 0")
    @DecimalMax(value = "2.0", message = "Điểm tính ứng dụng thực tiễn phải <= 2.0")
    private Float practicalApplicationScore; // Tính ứng dụng thực tiễn (0-2)

    @DecimalMin(value = "0.0", message = "Điểm thưởng phải >= 0")
    @DecimalMax(value = "0.5", message = "Điểm thưởng phải <= 0.5")
    private Float bonusScore; // Điểm thưởng (0-0.5)

    // Giảng viên hướng dẫn (SUPERVISOR) - 6 tiêu chí
    @DecimalMin(value = "0.0", message = "Điểm ý thức, thái độ sinh viên phải >= 0")
    @DecimalMax(value = "1.0", message = "Điểm ý thức, thái độ sinh viên phải <= 1.0")
    private Float studentAttitudeScore; // Ý thức, thái độ sinh viên (0-1)

    @DecimalMin(value = "0.0", message = "Điểm khả năng xử lý vấn đề phải >= 0")
    @DecimalMax(value = "1.0", message = "Điểm khả năng xử lý vấn đề phải <= 1.0")
    private Float problemSolvingScore; // Khả năng xử lý vấn đề (0-1)

    @DecimalMin(value = "0.0", message = "Điểm hình thức trình bày phải >= 0")
    @DecimalMax(value = "1.5", message = "Điểm hình thức trình bày phải <= 1.5")
    private Float formatSupervisorScore; // Hình thức trình bày (0-1.5)

    @DecimalMin(value = "0.0", message = "Điểm thực hiện nội dung đề tài phải >= 0")
    @DecimalMax(value = "4.5", message = "Điểm thực hiện nội dung đề tài phải <= 4.5")
    private Float contentImplementationSupervisorScore; // Thực hiện nội dung đề tài (0-4.5)

    @DecimalMin(value = "0.0", message = "Điểm mối liên hệ vấn đề liên quan phải >= 0")
    @DecimalMax(value = "1.0", message = "Điểm mối liên hệ vấn đề liên quan phải <= 1.0")
    private Float relatedIssuesSupervisorScore; // Mối liên hệ vấn đề liên quan (0-1)

    @DecimalMin(value = "0.0", message = "Điểm tính ứng dụng thực tiễn phải >= 0")
    @DecimalMax(value = "1.0", message = "Điểm tính ứng dụng thực tiễn phải <= 1.0")
    private Float practicalApplicationSupervisorScore; // Tính ứng dụng thực tiễn (0-1)

    private String comments;
}
