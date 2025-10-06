package com.phenikaa.evalservice.controller;

import com.phenikaa.evalservice.dto.request.ComprehensiveEvaluationPDFRequest;
import com.phenikaa.evalservice.entity.ProjectEvaluation;
import com.phenikaa.evalservice.repository.ProjectEvaluationRepository;
import com.phenikaa.evalservice.service.ComprehensiveEvaluationPDFService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/eval-service/teacher")
@RequiredArgsConstructor
@Slf4j
public class ComprehensiveEvaluationPDFController {
    
    private final ComprehensiveEvaluationPDFService pdfService;
    private final ProjectEvaluationRepository evaluationRepository;
    
    /**
     * Tạo PDF tổng hợp đánh giá với Q&A
     */
    @PostMapping("/reports/comprehensive-evaluation-pdf")
    public ResponseEntity<byte[]> generateComprehensiveEvaluationPDF(@RequestBody ComprehensiveEvaluationPDFRequest request) {
        try {
            log.info("Generating comprehensive evaluation PDF for topic: {}, student: {}", 
                    request.getTopicId(), request.getStudentName());
            
            byte[] pdfBytes = pdfService.generateComprehensiveEvaluationPDF(request);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", 
                    "comprehensive_evaluation_" + request.getTopicId() + "_" + 
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf");
            headers.setContentLength(pdfBytes.length);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
                    
        } catch (Exception e) {
            log.error("Error generating comprehensive evaluation PDF: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Tạo PDF tổng hợp đánh giá từ topic ID
     */
    @GetMapping("/reports/comprehensive-evaluation-pdf/topic/{topicId}")
    public ResponseEntity<byte[]> generateComprehensiveEvaluationPDFFromTopic(@PathVariable Integer topicId) {
        try {
            log.info("Generating comprehensive evaluation PDF for topic: {}", topicId);
            byte[] pdfBytes = pdfService.generateComprehensiveEvaluationPDFFromTopic(topicId);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", 
                    "comprehensive_evaluation_" + topicId + "_" + 
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf");
            headers.setContentLength(pdfBytes.length);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
                    
        } catch (Exception e) {
            log.error("Error generating comprehensive evaluation PDF for topic {}: {}", topicId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Error: " + e.getMessage()).getBytes());
        }
    }
    
    /**
     * Test endpoint để kiểm tra service hoạt động
     */
    @GetMapping("/reports/comprehensive-evaluation-pdf/test/{topicId}")
    public ResponseEntity<String> testComprehensiveEvaluationPDF(@PathVariable Integer topicId) {
        try {
            log.info("Testing comprehensive evaluation PDF for topic: {}", topicId);
            
            // Lấy tất cả đánh giá của topic
            List<ProjectEvaluation> evaluations = evaluationRepository
                    .findAllByTopicIdOrderByType(topicId);
            
            if (evaluations.isEmpty()) {
                return ResponseEntity.ok("Không tìm thấy đánh giá nào cho đề tài: " + topicId);
            }
            
            // Đếm số lượng đánh giá theo loại
            long committeeCount = evaluations.stream()
                    .filter(e -> e.getEvaluationType() == ProjectEvaluation.EvaluationType.COMMITTEE)
                    .count();
            long reviewerCount = evaluations.stream()
                    .filter(e -> e.getEvaluationType() == ProjectEvaluation.EvaluationType.REVIEWER)
                    .count();
            long supervisorCount = evaluations.stream()
                    .filter(e -> e.getEvaluationType() == ProjectEvaluation.EvaluationType.SUPERVISOR)
                    .count();
            
            String result = String.format(
                    "Đề tài %d có %d đánh giá:\n" +
                    "- Hội đồng: %d\n" +
                    "- Phản biện: %d\n" +
                    "- Hướng dẫn: %d",
                    topicId, evaluations.size(), committeeCount, reviewerCount, supervisorCount
            );
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("Error testing comprehensive evaluation PDF for topic {}: {}", topicId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }
}
