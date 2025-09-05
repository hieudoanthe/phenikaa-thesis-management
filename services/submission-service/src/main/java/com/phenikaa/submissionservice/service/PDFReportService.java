package com.phenikaa.submissionservice.service;

import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.phenikaa.submissionservice.dto.request.EvaluationReportRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class PDFReportService {
    
    /**
     * Tạo báo cáo chấm điểm PDF
     */
    public byte[] generateEvaluationReportPDF(EvaluationReportRequest request) {
        try {
            log.info("Generating evaluation report PDF for topic: {}, student: {}", 
                    request.getTopicId(), request.getStudentName());
            
            String htmlContent = generateEvaluationReportHTML(request);
            return convertHTMLToPDF(htmlContent);
            
        } catch (Exception e) {
            log.error("Error generating evaluation report PDF: {}", e.getMessage(), e);
            throw new RuntimeException("Không thể tạo báo cáo PDF: " + e.getMessage());
        }
    }
    
    /**
     * Tạo HTML content cho báo cáo chấm điểm
     */
    private String generateEvaluationReportHTML(EvaluationReportRequest request) {
        StringBuilder html = new StringBuilder();
        
        html.append("<!DOCTYPE html>");
        html.append("<html><head>");
        html.append("<meta charset='UTF-8'>");
        html.append("<style>");
        html.append(getCSSStyles());
        html.append("</style>");
        html.append("</head><body>");
        
        // Header
        html.append("<div class='header'>");
        html.append("<h1>BÁO CÁO CHẤM ĐIỂM LUẬN VĂN TỐT NGHIỆP</h1>");
        html.append("<h2>TRƯỜNG ĐẠI HỌC PHENIKAA</h2>");
        html.append("</div>");
        
        // Thông tin cơ bản
        html.append("<div class='section'>");
        html.append("<h3>THÔNG TIN CƠ BẢN</h3>");
        html.append("<table class='info-table'>");
        html.append("<tr><td><strong>Sinh viên:</strong></td><td>").append(request.getStudentName()).append("</td></tr>");
        html.append("<tr><td><strong>Đề tài:</strong></td><td>").append(request.getTopicTitle()).append("</td></tr>");
        html.append("<tr><td><strong>Giảng viên hướng dẫn:</strong></td><td>").append(request.getSupervisorName()).append("</td></tr>");
        html.append("<tr><td><strong>Giảng viên phản biện:</strong></td><td>").append(request.getReviewerName()).append("</td></tr>");
        html.append("<tr><td><strong>Thành viên hội đồng:</strong></td><td>").append(request.getCommitteeMembers()).append("</td></tr>");
        html.append("<tr><td><strong>Ngày bảo vệ:</strong></td><td>").append(request.getDefenseDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))).append("</td></tr>");
        html.append("<tr><td><strong>Giờ bảo vệ:</strong></td><td>").append(request.getDefenseTime()).append("</td></tr>");
        html.append("<tr><td><strong>Phòng bảo vệ:</strong></td><td>").append(request.getDefenseRoom()).append("</td></tr>");
        html.append("</table>");
        html.append("</div>");
        
        // Điểm số tổng hợp
        html.append("<div class='section'>");
        html.append("<h3>ĐIỂM SỐ TỔNG HỢP</h3>");
        html.append("<table class='score-table'>");
        html.append("<tr><th>Vai trò</th><th>Điểm số</th><th>Tỷ lệ</th><th>Điểm quy đổi</th></tr>");
        html.append("<tr><td>Giảng viên hướng dẫn</td><td>").append(formatScore(request.getSupervisorScore())).append("</td><td>25%</td><td>").append(formatScore(request.getSupervisorScore() * 0.25)).append("</td></tr>");
        html.append("<tr><td>Giảng viên phản biện</td><td>").append(formatScore(request.getReviewerScore())).append("</td><td>25%</td><td>").append(formatScore(request.getReviewerScore() * 0.25)).append("</td></tr>");
        html.append("<tr><td>Hội đồng</td><td>").append(formatScore(request.getCommitteeScore())).append("</td><td>50%</td><td>").append(formatScore(request.getCommitteeScore() * 0.5)).append("</td></tr>");
        html.append("<tr class='total-row'><td><strong>TỔNG ĐIỂM</strong></td><td colspan='3'><strong>").append(formatScore(request.getFinalScore())).append("</strong></td></tr>");
        html.append("</table>");
        html.append("</div>");
        
        // Chi tiết điểm số
        if (request.getSupervisorDetails() != null) {
            html.append(generateScoreDetailsHTML("GIẢNG VIÊN HƯỚNG DẪN", request.getSupervisorDetails()));
        }
        
        if (request.getReviewerDetails() != null) {
            html.append(generateScoreDetailsHTML("GIẢNG VIÊN PHẢN BIỆN", request.getReviewerDetails()));
        }
        
        if (request.getCommitteeDetails() != null) {
            html.append(generateScoreDetailsHTML("HỘI ĐỒNG", request.getCommitteeDetails()));
        }
        
        // Footer
        html.append("<div class='footer'>");
        html.append("<p>Báo cáo được tạo tự động bởi hệ thống quản lý luận văn tốt nghiệp</p>");
        html.append("<p>Ngày tạo: ").append(java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))).append("</p>");
        html.append("</div>");
        
        html.append("</body></html>");
        
        return html.toString();
    }
    
    /**
     * Tạo HTML cho chi tiết điểm số
     */
    private String generateScoreDetailsHTML(String title, Object details) {
        StringBuilder html = new StringBuilder();
        
        html.append("<div class='section'>");
        html.append("<h3>").append(title).append("</h3>");
        html.append("<table class='details-table'>");
        
        if (details instanceof EvaluationReportRequest.SupervisorScoreDetails) {
            EvaluationReportRequest.SupervisorScoreDetails supervisor = (EvaluationReportRequest.SupervisorScoreDetails) details;
            html.append("<tr><td>Ý thức và thái độ sinh viên</td><td>").append(formatScore(supervisor.getStudentAttitudeScore())).append("/1.0</td></tr>");
            html.append("<tr><td>Khả năng xử lý vấn đề</td><td>").append(formatScore(supervisor.getProblemSolvingScore())).append("/1.0</td></tr>");
            html.append("<tr><td>Hình thức trình bày</td><td>").append(formatScore(supervisor.getFormatSupervisorScore())).append("/1.5</td></tr>");
            html.append("<tr><td>Thực hiện nội dung đề tài</td><td>").append(formatScore(supervisor.getContentImplementationSupervisorScore())).append("/4.5</td></tr>");
            html.append("<tr><td>Mối liên hệ vấn đề liên quan</td><td>").append(formatScore(supervisor.getRelatedIssuesSupervisorScore())).append("/1.0</td></tr>");
            html.append("<tr><td>Tính ứng dụng thực tiễn</td><td>").append(formatScore(supervisor.getPracticalApplicationSupervisorScore())).append("/1.0</td></tr>");
            html.append("<tr class='subtotal-row'><td><strong>Tổng điểm</strong></td><td><strong>").append(formatScore(supervisor.getTotalScore())).append("/10.0</strong></td></tr>");
        }
        // Thêm các loại details khác nếu cần
        
        html.append("</table>");
        html.append("</div>");
        
        return html.toString();
    }
    
    /**
     * Chuyển đổi HTML sang PDF
     */
    private byte[] convertHTMLToPDF(String htmlContent) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        
        try (PdfWriter writer = new PdfWriter(outputStream);
             PdfDocument pdf = new PdfDocument(writer)) {
            
            HtmlConverter.convertToPdf(htmlContent, writer);
        }
        
        return outputStream.toByteArray();
    }
    
    /**
     * CSS styles cho PDF
     */
    private String getCSSStyles() {
        return """
            body { font-family: Arial, sans-serif; margin: 20px; line-height: 1.6; }
            .header { text-align: center; margin-bottom: 30px; border-bottom: 2px solid #333; padding-bottom: 20px; }
            .header h1 { color: #2c3e50; margin: 0; font-size: 24px; }
            .header h2 { color: #7f8c8d; margin: 5px 0 0 0; font-size: 18px; }
            .section { margin: 20px 0; }
            .section h3 { color: #2c3e50; border-bottom: 1px solid #bdc3c7; padding-bottom: 5px; }
            .info-table, .score-table, .details-table { width: 100%; border-collapse: collapse; margin: 10px 0; }
            .info-table td, .score-table td, .details-table td { padding: 8px; border: 1px solid #ddd; }
            .info-table td:first-child, .score-table td:first-child, .details-table td:first-child { 
                background-color: #f8f9fa; font-weight: bold; width: 30%; }
            .score-table th, .details-table th { background-color: #34495e; color: white; padding: 10px; }
            .total-row, .subtotal-row { background-color: #ecf0f1; font-weight: bold; }
            .footer { margin-top: 40px; text-align: center; font-size: 12px; color: #7f8c8d; border-top: 1px solid #bdc3c7; padding-top: 20px; }
            """;
    }
    
    /**
     * Format điểm số
     */
    private String formatScore(Double score) {
        if (score == null) return "N/A";
        return String.format("%.2f", score);
    }
}
