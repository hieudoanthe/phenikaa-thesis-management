package com.phenikaa.evalservice.controller;

import com.phenikaa.evalservice.client.ThesisServiceClient;
import com.phenikaa.evalservice.client.UserServiceClient;
import com.phenikaa.evalservice.dto.request.ComprehensiveEvaluationPDFRequest;
import com.phenikaa.evalservice.dto.response.QnAResponse;
import com.phenikaa.evalservice.entity.ProjectEvaluation;
import com.phenikaa.evalservice.exception.PDFGenerationException;
import com.phenikaa.evalservice.repository.ProjectEvaluationRepository;
import com.phenikaa.evalservice.service.ComprehensiveEvaluationPDFService;
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

@RestController
@RequestMapping("/api/eval-service/teacher")
@RequiredArgsConstructor
@Slf4j
public class ComprehensiveEvaluationPDFController {
    
    private final ComprehensiveEvaluationPDFService pdfService;
    private final ProjectEvaluationRepository evaluationRepository;
    private final DefenseQnAService qnAService;
    private final ThesisServiceClient thesisServiceClient;
    private final UserServiceClient userServiceClient;
    
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
            
            // Lấy tất cả đánh giá của topic
            List<ProjectEvaluation> evaluations = evaluationRepository
                    .findAllByTopicIdOrderByType(topicId);
            
            if (evaluations.isEmpty()) {
                log.warn("No evaluations found for topic: {}", topicId);
                return ResponseEntity.notFound().build();
            }
            
            // Tạo request từ evaluations
            ComprehensiveEvaluationPDFRequest request = createRequestFromEvaluations(topicId, evaluations);
            
            byte[] pdfBytes = pdfService.generateComprehensiveEvaluationPDF(request);
            
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
     * Tạo request từ danh sách evaluations
     */
    private ComprehensiveEvaluationPDFRequest createRequestFromEvaluations(Integer topicId, List<ProjectEvaluation> evaluations) {
        try {
            ComprehensiveEvaluationPDFRequest request = new ComprehensiveEvaluationPDFRequest();
            request.setTopicId(topicId);
            
            // Lấy thông tin cơ bản từ evaluation đầu tiên
            ProjectEvaluation firstEvaluation = evaluations.get(0);
            request.setStudentName("Sinh viên " + firstEvaluation.getStudentId());
            request.setStudentIdNumber("SV" + firstEvaluation.getStudentId());
            request.setClassName("K15-CNTT4");
            request.setMajor("Công nghệ thông tin");
            request.setBatch("K15");
            request.setTopicTitle("Đề tài " + topicId);
            request.setEvaluationDate(firstEvaluation.getEvaluatedAt() != null ? firstEvaluation.getEvaluatedAt() : LocalDateTime.now());
            
            // Phân loại evaluations theo loại
            int committeeCount = 0;
            for (ProjectEvaluation evaluation : evaluations) {
                switch (evaluation.getEvaluationType()) {
                    case COMMITTEE:
                        committeeCount++;
                        if (committeeCount == 1) {
                            request.setChairman(createCommitteeMember(evaluation, "Chủ tịch hội đồng"));
                        } else if (committeeCount == 2) {
                            request.setSecretary(createCommitteeMember(evaluation, "Thư ký hội đồng"));
                        } else if (committeeCount == 3) {
                            request.setMember(createCommitteeMember(evaluation, "Thành viên hội đồng"));
                        }
                        break;
                    case REVIEWER:
                        request.setReviewer(createReviewer(evaluation));
                        break;
                    case SUPERVISOR:
                        request.setSupervisor(createSupervisor(evaluation));
                        break;
                }
            }
            
            // Lấy Q&A data
            try {
                List<QnAResponse> qnaList = qnAService.getQnAByTopicAndStudent(topicId, firstEvaluation.getStudentId());
                if (qnaList != null && !qnaList.isEmpty()) {
                    List<ComprehensiveEvaluationPDFRequest.QnAData> qnaData = qnaList.stream()
                            .map(qna -> {
                                ComprehensiveEvaluationPDFRequest.QnAData data = new ComprehensiveEvaluationPDFRequest.QnAData();
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
            } catch (Exception e) {
                log.warn("Error fetching Q&A data for topic {}: {}", topicId, e.getMessage());
            }
            
            // Lấy thông tin chi tiết từ các service khác
            try {
                // Lấy thông tin đề tài từ thesis-service
                Map<String, Object> topicInfo = thesisServiceClient.getTopicById(topicId);
                if (topicInfo != null && !topicInfo.isEmpty()) {
                    request.setTopicTitle((String) topicInfo.get("title"));
                } else {
                    log.warn("Could not fetch topic info for topicId: {}", topicId);
                    request.setTopicTitle("Đề tài " + topicId);
                }
            } catch (Exception e) {
                log.warn("Error fetching topic info for topicId {}: {}", topicId, e.getMessage());
                request.setTopicTitle("Đề tài " + topicId);
            }

            try {
                // Lấy thông tin sinh viên từ user-service
                Map<String, Object> userInfo = userServiceClient.getUserById(firstEvaluation.getStudentId());
                if (userInfo != null && !userInfo.isEmpty()) {
                    request.setStudentName((String) userInfo.get("fullName"));
                    request.setStudentIdNumber("SV" + firstEvaluation.getStudentId());
                } else {
                    log.warn("Could not fetch user info for studentId: {}", firstEvaluation.getStudentId());
                    request.setStudentName("Sinh viên " + firstEvaluation.getStudentId());
                    request.setStudentIdNumber("SV" + firstEvaluation.getStudentId());
                }
            } catch (Exception e) {
                log.warn("Error fetching user info for studentId {}: {}", firstEvaluation.getStudentId(), e.getMessage());
                request.setStudentName("Sinh viên " + firstEvaluation.getStudentId());
                request.setStudentIdNumber("SV" + firstEvaluation.getStudentId());
            }
            
            return request;
        } catch (Exception e) {
            log.error("Error creating comprehensive PDF request from evaluations: {}", e.getMessage(), e);
            throw new PDFGenerationException("Không thể tạo request PDF tổng hợp: " + e.getMessage(), e);
        }
    }
    
    /**
     * Tạo CommitteeMember từ ProjectEvaluation
     */
    private ComprehensiveEvaluationPDFRequest.CommitteeMember createCommitteeMember(ProjectEvaluation evaluation, String role) {
        ComprehensiveEvaluationPDFRequest.CommitteeMember member = new ComprehensiveEvaluationPDFRequest.CommitteeMember();
        member.setName("ThS. " + role);
        member.setTitle("Thạc sĩ");
        member.setDepartment("Khoa Hệ thống thông tin");
        member.setPresentationClarityScore(evaluation.getPresentationClarityScore());
        member.setReviewerQaScore(evaluation.getReviewerQaScore());
        member.setCommitteeQaScore(evaluation.getCommitteeQaScore());
        member.setAttitudeScore(evaluation.getAttitudeScore());
        member.setContentImplementationScore(evaluation.getContentImplementationScore());
        member.setRelatedIssuesScore(evaluation.getRelatedIssuesScore());
        member.setTotalScore(evaluation.getTotalScore());
        member.setComments(evaluation.getComments());
        return member;
    }
    
    /**
     * Tạo Reviewer từ ProjectEvaluation
     */
    private ComprehensiveEvaluationPDFRequest.Reviewer createReviewer(ProjectEvaluation evaluation) {
        ComprehensiveEvaluationPDFRequest.Reviewer reviewer = new ComprehensiveEvaluationPDFRequest.Reviewer();
        reviewer.setName("ThS. Giảng viên phản biện");
        reviewer.setTitle("Thạc sĩ");
        reviewer.setDepartment("Khoa Hệ thống thông tin");
        reviewer.setPresentationFormatScore(evaluation.getFormatScore());
        reviewer.setContentImplementationScore(evaluation.getContentQualityScore());
        reviewer.setRelatedIssuesScore(evaluation.getRelatedIssuesReviewerScore());
        reviewer.setPracticalApplicationScore(evaluation.getPracticalApplicationScore());
        reviewer.setBonusScore(evaluation.getBonusScore());
        reviewer.setTotalScore(evaluation.getTotalScore());
        reviewer.setComments(evaluation.getComments());
        return reviewer;
    }
    
    /**
     * Tạo Supervisor từ ProjectEvaluation
     */
    private ComprehensiveEvaluationPDFRequest.Supervisor createSupervisor(ProjectEvaluation evaluation) {
        ComprehensiveEvaluationPDFRequest.Supervisor supervisor = new ComprehensiveEvaluationPDFRequest.Supervisor();
        supervisor.setName("ThS. Giảng viên hướng dẫn");
        supervisor.setTitle("Thạc sĩ");
        supervisor.setDepartment("Khoa Hệ thống thông tin");
        supervisor.setAttitudeScore(evaluation.getStudentAttitudeScore());
        supervisor.setProblemSolvingScore(evaluation.getProblemSolvingScore());
        supervisor.setPresentationFormatScore(evaluation.getFormatSupervisorScore());
        supervisor.setContentImplementationScore(evaluation.getContentImplementationSupervisorScore());
        supervisor.setRelatedIssuesScore(evaluation.getRelatedIssuesSupervisorScore());
        supervisor.setPracticalApplicationScore(evaluation.getPracticalApplicationSupervisorScore());
        supervisor.setTotalScore(evaluation.getTotalScore());
        supervisor.setComments(evaluation.getComments());
        return supervisor;
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
