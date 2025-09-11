package com.phenikaa.evalservice.repository;

import com.phenikaa.evalservice.entity.ProjectEvaluation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectEvaluationRepository extends JpaRepository<ProjectEvaluation, Integer> {
    
    // Tìm đánh giá theo topic và loại đánh giá
    List<ProjectEvaluation> findByTopicIdAndEvaluationType(Integer topicId, ProjectEvaluation.EvaluationType evaluationType);
    
    // Tìm đánh giá theo sinh viên
    List<ProjectEvaluation> findByStudentId(Integer studentId);
    
    // Tìm đánh giá theo giảng viên
    List<ProjectEvaluation> findByEvaluatorId(Integer evaluatorId);
    
    // Tìm đánh giá theo topic
    List<ProjectEvaluation> findByTopicId(Integer topicId);
    
    // Tìm đánh giá theo trạng thái
    List<ProjectEvaluation> findByEvaluationStatus(ProjectEvaluation.EvaluationStatus status);
    
    // Kiểm tra xem đã có đánh giá chưa
    boolean existsByTopicIdAndEvaluatorIdAndEvaluationType(Integer topicId, Integer evaluatorId, ProjectEvaluation.EvaluationType evaluationType);
    
    // Tìm tất cả đánh giá cụ thể (trong trường hợp có nhiều bản ghi) và sắp xếp mới nhất trước
    List<ProjectEvaluation> findAllByTopicIdAndEvaluatorIdAndEvaluationTypeOrderByEvaluatedAtDesc(Integer topicId, Integer evaluatorId, ProjectEvaluation.EvaluationType evaluationType);
    
    // Lấy tất cả đánh giá của một topic
    @Query("SELECT pe FROM ProjectEvaluation pe WHERE pe.topicId = :topicId ORDER BY pe.evaluationType, pe.evaluatedAt")
    List<ProjectEvaluation> findAllByTopicIdOrderByType(@Param("topicId") Integer topicId);
    
    // Tính điểm trung bình theo loại đánh giá
    @Query("SELECT AVG(pe.totalScore) FROM ProjectEvaluation pe WHERE pe.topicId = :topicId AND pe.evaluationType = :evaluationType")
    Double getAverageScoreByTopicAndType(@Param("topicId") Integer topicId, @Param("evaluationType") ProjectEvaluation.EvaluationType evaluationType);
    
    // Lấy đánh giá theo loại và topic
    @Query("SELECT pe FROM ProjectEvaluation pe WHERE pe.topicId = :topicId AND pe.evaluationType = :evaluationType")
    List<ProjectEvaluation> findByTopicIdAndEvaluationTypeList(@Param("topicId") Integer topicId, @Param("evaluationType") ProjectEvaluation.EvaluationType evaluationType);
    
    // Thống kê - đếm theo loại đánh giá
    long countByEvaluationType(ProjectEvaluation.EvaluationType evaluationType);
    
    // Thống kê - điểm trung bình tổng
    @Query("SELECT AVG(pe.totalScore) FROM ProjectEvaluation pe WHERE pe.totalScore IS NOT NULL")
    Double findAverageScore();
}
