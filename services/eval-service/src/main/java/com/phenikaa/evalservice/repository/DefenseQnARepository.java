package com.phenikaa.evalservice.repository;

import com.phenikaa.evalservice.entity.DefenseQnA;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DefenseQnARepository extends JpaRepository<DefenseQnA, Integer> {
    
    // Tìm Q&A theo topic
    List<DefenseQnA> findByTopicId(Integer topicId);
    
    // Tìm Q&A theo sinh viên
    List<DefenseQnA> findByStudentId(Integer studentId);
    
    // Tìm Q&A theo người hỏi
    List<DefenseQnA> findByQuestionerId(Integer questionerId);
    
    // Tìm Q&A theo thư ký
    List<DefenseQnA> findBySecretaryId(Integer secretaryId);
    
    // Tìm Q&A theo topic và sinh viên
    List<DefenseQnA> findByTopicIdAndStudentId(Integer topicId, Integer studentId);
    
    // Lấy Q&A theo topic, sắp xếp theo thời gian
    @Query("SELECT qna FROM DefenseQnA qna WHERE qna.topicId = :topicId ORDER BY qna.questionTime ASC")
    List<DefenseQnA> findByTopicIdOrderByQuestionTime(@Param("topicId") Integer topicId);
}
