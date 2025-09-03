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

    @DecimalMin(value = "0.0", message = "Điểm nội dung phải >= 0")
    @DecimalMax(value = "10.0", message = "Điểm nội dung phải <= 10")
    private Float contentScore;

    @DecimalMin(value = "0.0", message = "Điểm thuyết trình phải >= 0")
    @DecimalMax(value = "10.0", message = "Điểm thuyết trình phải <= 10")
    private Float presentationScore;

    @DecimalMin(value = "0.0", message = "Điểm kỹ thuật phải >= 0")
    @DecimalMax(value = "10.0", message = "Điểm kỹ thuật phải <= 10")
    private Float technicalScore;

    @DecimalMin(value = "0.0", message = "Điểm sáng tạo phải >= 0")
    @DecimalMax(value = "10.0", message = "Điểm sáng tạo phải <= 10")
    private Float innovationScore;

    // Chỉ áp dụng cho hội đồng
    @DecimalMin(value = "0.0", message = "Điểm bảo vệ phải >= 0")
    @DecimalMax(value = "10.0", message = "Điểm bảo vệ phải <= 10")
    private Float defenseScore;

    private String comments;
}
