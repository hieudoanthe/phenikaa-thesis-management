package com.phenikaa.submissionservice.controller;

import com.phenikaa.submissionservice.dto.request.EvaluationReportRequest;
import com.phenikaa.submissionservice.service.PDFReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/submission-service")
@RequiredArgsConstructor
@Slf4j
public class PDFReportController {
    
    private final PDFReportService pdfReportService;
    
    /**
     * Tạo báo cáo chấm điểm PDF
     */
    @PostMapping("/reports/evaluation-pdf")
    public ResponseEntity<byte[]> generateEvaluationReportPDF(@RequestBody EvaluationReportRequest request) {
        try {
            log.info("Generating evaluation report PDF for topic: {}, student: {}", 
                    request.getTopicId(), request.getStudentName());
            
            byte[] pdfBytes = pdfReportService.generateEvaluationReportPDF(request);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", 
                    "evaluation_report_" + request.getTopicId() + "_" + 
                    request.getStudentId() + "_" + 
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf");
            headers.setContentLength(pdfBytes.length);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
                    
        } catch (Exception e) {
            log.error("Error generating evaluation report PDF: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Tạo báo cáo chấm điểm PDF với dữ liệu mẫu (để test)
     */
    @GetMapping("/reports/evaluation-pdf/sample")
    public ResponseEntity<byte[]> generateSampleEvaluationReportPDF() {
        try {
            log.info("Generating sample evaluation report PDF");
            
            // Tạo dữ liệu mẫu
            EvaluationReportRequest sampleRequest = createSampleEvaluationReport();
            byte[] pdfBytes = pdfReportService.generateEvaluationReportPDF(sampleRequest);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "sample_evaluation_report.pdf");
            headers.setContentLength(pdfBytes.length);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
                    
        } catch (Exception e) {
            log.error("Error generating sample evaluation report PDF: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Tạo dữ liệu mẫu cho báo cáo chấm điểm
     */
    private EvaluationReportRequest createSampleEvaluationReport() {
        // Tạo chi tiết điểm số giảng viên hướng dẫn
        EvaluationReportRequest.SupervisorScoreDetails supervisorDetails = 
                EvaluationReportRequest.SupervisorScoreDetails.builder()
                        .studentAttitudeScore(0.8)
                        .problemSolvingScore(0.9)
                        .formatSupervisorScore(1.3)
                        .contentImplementationSupervisorScore(4.2)
                        .relatedIssuesSupervisorScore(0.9)
                        .practicalApplicationSupervisorScore(0.8)
                        .totalScore(8.9)
                        .build();
        
        // Tạo chi tiết điểm số giảng viên phản biện
        EvaluationReportRequest.ReviewerScoreDetails reviewerDetails = 
                EvaluationReportRequest.ReviewerScoreDetails.builder()
                        .formatScore(1.4)
                        .contentQualityScore(3.8)
                        .relatedIssuesReviewerScore(1.9)
                        .practicalApplicationScore(1.8)
                        .bonusScore(0.3)
                        .totalScore(9.2)
                        .build();
        
        // Tạo chi tiết điểm số hội đồng
        EvaluationReportRequest.CommitteeScoreDetails committeeDetails = 
                EvaluationReportRequest.CommitteeScoreDetails.builder()
                        .presentationClarityScore(0.4)
                        .reviewerQaScore(1.4)
                        .committeeQaScore(1.3)
                        .attitudeScore(0.9)
                        .contentImplementationScore(4.1)
                        .relatedIssuesScore(0.8)
                        .totalScore(8.9)
                        .build();
        
        return EvaluationReportRequest.builder()
                .topicId(1)
                .studentId(111)
                .studentName("Nguyễn Văn A")
                .topicTitle("Hệ thống quản lý luận văn tốt nghiệp")
                .supervisorName("TS. Trần Thị B")
                .reviewerName("PGS.TS. Lê Văn C")
                .committeeMembers("TS. Phạm Thị D, ThS. Nguyễn Văn E, TS. Hoàng Thị F")
                .defenseDate(LocalDateTime.now().plusDays(7))
                .defenseTime("09:00")
                .defenseRoom("Phòng 301 - Tòa A")
                .supervisorScore(8.9)
                .reviewerScore(9.2)
                .committeeScore(8.9)
                .finalScore(8.95)
                .supervisorDetails(supervisorDetails)
                .reviewerDetails(reviewerDetails)
                .committeeDetails(committeeDetails)
                .reportTitle("Báo cáo luận văn tốt nghiệp")
                .reportDescription("Báo cáo về hệ thống quản lý luận văn tốt nghiệp")
                .reportFilePath("/uploads/reports/topic_1/report_111.pdf")
                .reportSubmittedAt(LocalDateTime.now().minusDays(3))
                .academicYear("2024-2025")
                .semester("Học kỳ 2")
                .department("Khoa Công nghệ thông tin")
                .major("Công nghệ thông tin")
                .build();
    }
}
