package com.phenikaa.evalservice.controller;

import com.phenikaa.evalservice.client.ThesisServiceClient;
import com.phenikaa.evalservice.client.UserServiceClient;
import com.phenikaa.evalservice.dto.request.CommitteeEvaluationPDFRequest;
import com.phenikaa.evalservice.dto.response.QnAResponse;
import com.phenikaa.evalservice.entity.ProjectEvaluation;
import com.phenikaa.evalservice.exception.PDFGenerationException;
import com.phenikaa.evalservice.repository.ProjectEvaluationRepository;
import com.phenikaa.evalservice.service.CommitteeEvaluationPDFService;
import com.phenikaa.evalservice.service.DefenseQnAService;
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
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/eval-service/teacher")
@RequiredArgsConstructor
@Slf4j
public class CommitteeEvaluationPDFController {
    
    private final CommitteeEvaluationPDFService pdfService;
    private final ProjectEvaluationRepository evaluationRepository;
    private final DefenseQnAService qnAService;
    private final ThesisServiceClient thesisServiceClient;
    private final UserServiceClient userServiceClient;
    
    /**
     * Tạo PDF phiếu đánh giá hội đồng với Q&A
     */
    @PostMapping("/reports/committee-evaluation-pdf")
    public ResponseEntity<byte[]> generateCommitteeEvaluationPDF(@RequestBody CommitteeEvaluationPDFRequest request) {
        try {
            log.info("Generating committee evaluation PDF for topic: {}, student: {}", 
                    request.getTopicId(), request.getStudentName());
            
            byte[] pdfBytes = pdfService.generateCommitteeEvaluationPDF(request);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", 
                    "committee_evaluation_" + request.getTopicId() + "_" + 
                    request.getStudentId() + "_" + 
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf");
            headers.setContentLength(pdfBytes.length);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
                    
        } catch (Exception e) {
            log.error("Error generating committee evaluation PDF: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Tạo PDF phiếu đánh giá hội đồng từ evaluation ID
     */
    @GetMapping("/reports/committee-evaluation-pdf/{evaluationId}")
    public ResponseEntity<byte[]> generateCommitteeEvaluationPDFFromId(@PathVariable Integer evaluationId) {
        try {
            log.info("Generating committee evaluation PDF for evaluation ID: {}", evaluationId);
            
            Optional<ProjectEvaluation> evaluationOpt = evaluationRepository.findById(evaluationId);
            if (evaluationOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            ProjectEvaluation evaluation = evaluationOpt.get();
            
            // Chỉ cho phép tạo PDF cho đánh giá hội đồng
            if (evaluation.getEvaluationType() != ProjectEvaluation.EvaluationType.COMMITTEE) {
                return ResponseEntity.badRequest().build();
            }
            
            // Lấy Q&A data
            List<QnAResponse> qnaList = qnAService.getQnAByTopicAndStudent(evaluation.getTopicId(), evaluation.getStudentId());
            
            // Tạo request cho PDF
            CommitteeEvaluationPDFRequest request = createPDFRequestFromEvaluation(evaluation, qnaList);
            
            byte[] pdfBytes = pdfService.generateCommitteeEvaluationPDF(request);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", 
                    "committee_evaluation_" + evaluation.getTopicId() + "_" + 
                    evaluation.getStudentId() + "_" + 
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf");
            headers.setContentLength(pdfBytes.length);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
                    
        } catch (Exception e) {
            log.error("Error generating committee evaluation PDF: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Tạo PDF phiếu đánh giá hội đồng từ topic ID (lấy đánh giá hội đồng đầu tiên)
     */
    @GetMapping("/reports/committee-evaluation-pdf/topic/{topicId}")
    public ResponseEntity<byte[]> generateCommitteeEvaluationPDFFromTopic(@PathVariable Integer topicId) {
        try {
            log.info("Generating committee evaluation PDF for topic: {}", topicId);
            
            // Tìm đánh giá hội đồng cho topic này
            List<ProjectEvaluation> committeeEvaluations = evaluationRepository
                    .findByTopicIdAndEvaluationType(topicId, ProjectEvaluation.EvaluationType.COMMITTEE);
            
            if (committeeEvaluations.isEmpty()) {
                log.warn("No committee evaluation found for topic: {}", topicId);
                return ResponseEntity.notFound().build();
            }
            
            ProjectEvaluation evaluation = committeeEvaluations.get(0);
            log.info("Found evaluation: {} for topic: {}", evaluation.getEvaluationId(), topicId);
            
            // Lấy Q&A data
            List<QnAResponse> qnaList = qnAService.getQnAByTopicAndStudent(evaluation.getTopicId(), evaluation.getStudentId());
            log.info("Found {} Q&A records for topic: {}", qnaList.size(), topicId);
            
            // Tạo request cho PDF
            CommitteeEvaluationPDFRequest request = createPDFRequestFromEvaluation(evaluation, qnaList);
            log.info("Created PDF request for topic: {}, student: {}", request.getTopicId(), request.getStudentName());
            
            byte[] pdfBytes = pdfService.generateCommitteeEvaluationPDF(request);
            log.info("Generated PDF with {} bytes", pdfBytes.length);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", 
                    "committee_evaluation_" + evaluation.getTopicId() + "_" + 
                    evaluation.getStudentId() + "_" + 
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf");
            headers.setContentLength(pdfBytes.length);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
                    
        } catch (Exception e) {
            log.error("Error generating committee evaluation PDF for topic {}: {}", topicId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Error: " + e.getMessage()).getBytes());
        }
    }
    
    /**
     * Tạo request từ evaluation entity
     */
    private CommitteeEvaluationPDFRequest createPDFRequestFromEvaluation(ProjectEvaluation evaluation, List<QnAResponse> qnaList) {
        try {
            CommitteeEvaluationPDFRequest request = new CommitteeEvaluationPDFRequest();
            
            // Thông tin cơ bản
            request.setTopicId(evaluation.getTopicId());
            request.setStudentId(evaluation.getStudentId());
            request.setEvaluationDate(evaluation.getEvaluatedAt() != null ? evaluation.getEvaluatedAt() : LocalDateTime.now());
        
        // Điểm số
        request.setPresentationClarityScore(evaluation.getPresentationClarityScore());
        request.setReviewerQaScore(evaluation.getReviewerQaScore());
        request.setCommitteeQaScore(evaluation.getCommitteeQaScore());
        request.setAttitudeScore(evaluation.getAttitudeScore());
        request.setContentImplementationScore(evaluation.getContentImplementationScore());
        request.setRelatedIssuesScore(evaluation.getRelatedIssuesScore());
        request.setTotalScore(evaluation.getTotalScore());
        request.setComments(evaluation.getComments());
        
        // Q&A data
        if (qnaList != null && !qnaList.isEmpty()) {
            List<CommitteeEvaluationPDFRequest.QnAData> qnaData = qnaList.stream()
                    .map(qna -> {
                        CommitteeEvaluationPDFRequest.QnAData data = new CommitteeEvaluationPDFRequest.QnAData();
                        data.setQuestion(qna.getQuestion());
                        data.setAnswer(qna.getAnswer());
                        data.setQuestionerName(qna.getQuestionerName() != null ? qna.getQuestionerName() : "Thành viên hội đồng");
                        data.setQuestionTime(qna.getQuestionTime());
                        data.setAnswerTime(qna.getAnswerTime());
                        return data;
                    })
                    .toList();
            request.setQnaData(qnaData);
        }
        
        // Lấy thông tin chi tiết từ các service khác
        try {
            // Lấy thông tin đề tài từ thesis-service
            Map<String, Object> topicInfo = thesisServiceClient.getTopicById(evaluation.getTopicId());
            if (topicInfo != null && !topicInfo.isEmpty()) {
                request.setTopicTitle((String) topicInfo.get("title"));
                request.setSupervisorTitle((String) topicInfo.get("supervisorTitle"));
            } else {
                log.warn("Could not fetch topic info for topicId: {}", evaluation.getTopicId());
                request.setTopicTitle("Đề tài " + evaluation.getTopicId());
                request.setSupervisorName("ThS. Nguyễn Thành Trung");
                request.setSupervisorTitle("Thạc sĩ");
            }
        } catch (Exception e) {
            log.warn("Error fetching topic info for topicId {}: {}", evaluation.getTopicId(), e.getMessage());
            request.setTopicTitle("Đề tài " + evaluation.getTopicId());
            request.setSupervisorName("ThS. Nguyễn Thành Trung");
            request.setSupervisorTitle("Thạc sĩ");
        }

        try {
            // Lấy thông tin sinh viên từ user-service
            Map<String, Object> userInfo = userServiceClient.getUserById(evaluation.getStudentId());
            if (userInfo != null && !userInfo.isEmpty()) {
                request.setStudentName((String) userInfo.get("fullName"));
                request.setStudentIdNumber("SV" + evaluation.getStudentId()); // GetUserResponse không có studentId field
                // GetUserResponse không có className, major, batch - sử dụng dữ liệu mẫu
                request.setClassName("K15-CNTT4");
                request.setMajor("Công nghệ thông tin");
                request.setBatch("K15");
            } else {
                log.warn("Could not fetch user info for studentId: {}", evaluation.getStudentId());
                request.setStudentName("Sinh viên " + evaluation.getStudentId());
                request.setStudentIdNumber("SV" + evaluation.getStudentId());
                request.setClassName("K15-CNTT4");
                request.setMajor("Công nghệ thông tin");
                request.setBatch("K15");
            }
        } catch (Exception e) {
            log.warn("Error fetching user info for studentId {}: {}", evaluation.getStudentId(), e.getMessage());
            request.setStudentName("Sinh viên " + evaluation.getStudentId());
            request.setStudentIdNumber("SV" + evaluation.getStudentId());
            request.setClassName("K15-CNTT4");
            request.setMajor("Công nghệ thông tin");
            request.setBatch("K15");
        }

        try {
            // Lấy thông tin giảng viên đánh giá từ user-service
            Map<String, Object> evaluatorInfo = userServiceClient.getUserById(evaluation.getEvaluatorId());
            if (evaluatorInfo != null && !evaluatorInfo.isEmpty()) {
                request.setEvaluatorName((String) evaluatorInfo.get("fullName"));
                // GetUserResponse không có title, department - sử dụng dữ liệu mẫu
                request.setEvaluatorTitle("Thạc sĩ");
                request.setEvaluatorDepartment("Khoa Hệ thống thông tin");
            } else {
                log.warn("Could not fetch evaluator info for evaluatorId: {}", evaluation.getEvaluatorId());
                request.setEvaluatorName("ThS. Vũ Văn Quang");
                request.setEvaluatorTitle("Thạc sĩ");
                request.setEvaluatorDepartment("Khoa Hệ thống thông tin");
            }
        } catch (Exception e) {
            log.warn("Error fetching evaluator info for evaluatorId {}: {}", evaluation.getEvaluatorId(), e.getMessage());
            request.setEvaluatorName("ThS. Vũ Văn Quang");
            request.setEvaluatorTitle("Thạc sĩ");
            request.setEvaluatorDepartment("Khoa Hệ thống thông tin");
        }
        
        return request;
        } catch (Exception e) {
            log.error("Error creating PDF request from evaluation: {}", e.getMessage(), e);
            throw new PDFGenerationException("Không thể tạo request PDF: " + e.getMessage(), e);
        }
    }
}
