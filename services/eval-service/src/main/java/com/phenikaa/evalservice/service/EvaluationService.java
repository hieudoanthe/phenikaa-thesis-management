package com.phenikaa.evalservice.service;

import com.phenikaa.evalservice.dto.request.EvaluationRequest;
import com.phenikaa.evalservice.dto.response.EvaluationResponse;
import com.phenikaa.evalservice.dto.response.FinalScoreResponse;
import com.phenikaa.evalservice.entity.ProjectEvaluation;
import com.phenikaa.evalservice.repository.ProjectEvaluationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EvaluationService {
    
    private final ProjectEvaluationRepository evaluationRepository;
    
    /**
     * Chấm điểm cho sinh viên
     */
    public EvaluationResponse submitEvaluation(EvaluationRequest request) {
        log.info("Submitting evaluation for topic: {}, student: {}, evaluator: {}, type: {}", 
                request.getTopicId(), request.getStudentId(), request.getEvaluatorId(), request.getEvaluationType());
        
        // Kiểm tra xem đã có đánh giá chưa
        var existingEvaluation = evaluationRepository
                .findByTopicIdAndEvaluatorIdAndEvaluationType(
                        request.getTopicId(), 
                        request.getEvaluatorId(), 
                        request.getEvaluationType()
                );
        
        ProjectEvaluation evaluation;
        if (existingEvaluation.isPresent()) {
            // Cập nhật đánh giá hiện có
            evaluation = existingEvaluation.get();
            log.info("Updating existing evaluation: {}", evaluation.getEvaluationId());
        } else {
            // Tạo đánh giá mới
            evaluation = new ProjectEvaluation();
            evaluation.setTopicId(request.getTopicId());
            evaluation.setStudentId(request.getStudentId());
            evaluation.setEvaluatorId(request.getEvaluatorId());
            evaluation.setEvaluationType(request.getEvaluationType());
            evaluation.setEvaluationStatus(ProjectEvaluation.EvaluationStatus.IN_PROGRESS);
        }
        
        // Cập nhật điểm số
        evaluation.setContentScore(request.getContentScore());
        evaluation.setPresentationScore(request.getPresentationScore());
        evaluation.setTechnicalScore(request.getTechnicalScore());
        evaluation.setInnovationScore(request.getInnovationScore());
        evaluation.setDefenseScore(request.getDefenseScore());
        evaluation.setComments(request.getComments());
        
        // Tính tổng điểm
        evaluation.calculateTotalScore();
        
        // Cập nhật trạng thái và thời gian
        evaluation.setEvaluationStatus(ProjectEvaluation.EvaluationStatus.COMPLETED);
        evaluation.setEvaluatedAt(LocalDateTime.now());
        
        // Lưu vào database
        ProjectEvaluation savedEvaluation = evaluationRepository.save(evaluation);
        
        log.info("Evaluation submitted successfully: {}", savedEvaluation.getEvaluationId());
        
        return convertToResponse(savedEvaluation);
    }
    
    /**
     * Lấy tất cả đánh giá của một topic
     */
    public List<EvaluationResponse> getEvaluationsByTopic(Integer topicId) {
        List<ProjectEvaluation> evaluations = evaluationRepository.findAllByTopicIdOrderByType(topicId);
        return evaluations.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Lấy đánh giá theo sinh viên
     */
    public List<EvaluationResponse> getEvaluationsByStudent(Integer studentId) {
        List<ProjectEvaluation> evaluations = evaluationRepository.findByStudentId(studentId);
        return evaluations.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Lấy đánh giá theo giảng viên
     */
    public List<EvaluationResponse> getEvaluationsByEvaluator(Integer evaluatorId) {
        List<ProjectEvaluation> evaluations = evaluationRepository.findByEvaluatorId(evaluatorId);
        return evaluations.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Tính điểm trung bình cuối cùng theo công thức: (GVHD x1 + GVPB x2 + HĐ x1) / 4
     */
    public FinalScoreResponse calculateFinalScore(Integer topicId) {
        log.info("Calculating final score for topic: {}", topicId);
        
        // Lấy tất cả đánh giá của topic
        List<ProjectEvaluation> evaluations = evaluationRepository.findByTopicId(topicId);
        
        if (evaluations.isEmpty()) {
            log.warn("No evaluations found for topic: {}", topicId);
            return null;
        }
        
        // Tính điểm trung bình cho từng loại đánh giá
        Double supervisorScore = getAverageScoreByType(evaluations, ProjectEvaluation.EvaluationType.SUPERVISOR);
        Double reviewerScore = getAverageScoreByType(evaluations, ProjectEvaluation.EvaluationType.REVIEWER);
        Double committeeScore = getAverageScoreByType(evaluations, ProjectEvaluation.EvaluationType.COMMITTEE);
        
        // Tính điểm cuối cùng: (GVHD x1 + GVPB x2 + HĐ x1) / 4
        Double finalScore = null;
        String status = "INCOMPLETE";
        
        if (supervisorScore != null && reviewerScore != null && committeeScore != null) {
            finalScore = (supervisorScore * 1 + reviewerScore * 2 + committeeScore * 1) / 4.0;
            status = "COMPLETED";
        } else if (supervisorScore != null || reviewerScore != null || committeeScore != null) {
            status = "INCOMPLETE";
        } else {
            status = "PENDING";
        }
        
        // Tạo response
        FinalScoreResponse response = new FinalScoreResponse();
        response.setTopicId(topicId);
        response.setSupervisorScore(supervisorScore != null ? supervisorScore.floatValue() : null);
        response.setReviewerScore(reviewerScore != null ? reviewerScore.floatValue() : null);
        response.setCommitteeScore(committeeScore != null ? committeeScore.floatValue() : null);
        response.setFinalScore(finalScore != null ? finalScore.floatValue() : null);
        response.setStatus(status);
        response.setEvaluations(evaluations.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList()));
        
        log.info("Final score calculated for topic {}: {}", topicId, finalScore);
        
        return response;
    }
    
    /**
     * Tính điểm trung bình theo loại đánh giá
     */
    private Double getAverageScoreByType(List<ProjectEvaluation> evaluations, ProjectEvaluation.EvaluationType type) {
        List<ProjectEvaluation> typeEvaluations = evaluations.stream()
                .filter(e -> e.getEvaluationType() == type && e.getTotalScore() != null)
                .collect(Collectors.toList());
        
        if (typeEvaluations.isEmpty()) {
            return null;
        }
        
        double sum = typeEvaluations.stream()
                .mapToDouble(ProjectEvaluation::getTotalScore)
                .sum();
        
        return sum / typeEvaluations.size();
    }
    
    /**
     * Convert entity to response
     */
    private EvaluationResponse convertToResponse(ProjectEvaluation evaluation) {
        EvaluationResponse response = new EvaluationResponse();
        response.setEvaluationId(evaluation.getEvaluationId());
        response.setTopicId(evaluation.getTopicId());
        response.setStudentId(evaluation.getStudentId());
        response.setEvaluatorId(evaluation.getEvaluatorId());
        response.setEvaluationType(evaluation.getEvaluationType());
        response.setContentScore(evaluation.getContentScore());
        response.setPresentationScore(evaluation.getPresentationScore());
        response.setTechnicalScore(evaluation.getTechnicalScore());
        response.setInnovationScore(evaluation.getInnovationScore());
        response.setDefenseScore(evaluation.getDefenseScore());
        response.setTotalScore(evaluation.getTotalScore());
        response.setComments(evaluation.getComments());
        response.setEvaluatedAt(evaluation.getEvaluatedAt());
        response.setEvaluationStatus(evaluation.getEvaluationStatus());
        
        // TODO: Lấy thông tin tên từ user-service và thesis-service
        // response.setStudentName(...);
        // response.setTopicTitle(...);
        // response.setEvaluatorName(...);
        
        return response;
    }
}
