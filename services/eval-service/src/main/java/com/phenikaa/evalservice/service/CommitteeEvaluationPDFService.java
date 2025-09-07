package com.phenikaa.evalservice.service;

import com.itextpdf.html2pdf.HtmlConverter;
import com.phenikaa.evalservice.dto.request.CommitteeEvaluationPDFRequest;
import com.phenikaa.evalservice.exception.PDFGenerationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommitteeEvaluationPDFService {
    
    /**
     * Tạo PDF phiếu đánh giá hội đồng
     */
    public byte[] generateCommitteeEvaluationPDF(CommitteeEvaluationPDFRequest request) {
        try {
            log.info("Generating committee evaluation PDF for topic: {}, student: {}", 
                    request.getTopicId(), request.getStudentName());
            
            // Validate request
            if (request.getTopicId() == null) {
                throw new IllegalArgumentException("Topic ID cannot be null");
            }
            if (request.getStudentName() == null) {
                request.setStudentName("Sinh viên " + request.getStudentId());
            }
            
            String htmlContent = generateCommitteeEvaluationHTML(request);
            log.info("Generated HTML content length: {}", htmlContent.length());
            
            byte[] pdfBytes = convertHTMLToPDF(htmlContent);
            log.info("Generated PDF with {} bytes", pdfBytes.length);
            
            return pdfBytes;
            
        } catch (PDFGenerationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error generating committee evaluation PDF: {}", e.getMessage(), e);
            throw new PDFGenerationException("Không thể tạo PDF phiếu đánh giá: " + e.getMessage(), e);
        }
    }
    
    /**
     * Tạo HTML content cho phiếu đánh giá hội đồng
     */
    private String generateCommitteeEvaluationHTML(CommitteeEvaluationPDFRequest request) {
        try {
            StringBuilder html = new StringBuilder();
            
            html.append("<!DOCTYPE html>");
            html.append("<html>");
            html.append("<head>");
            html.append("<meta charset='UTF-8'>");
            html.append("<title>Phiếu đánh giá đồ án/khóa luận tốt nghiệp</title>");
            html.append(getCSSStyles());
            html.append("</head>");
            html.append("<body>");
        
        // Header
        html.append("<div class='header'>");
        html.append("<div class='header-left'>");
        html.append("<p><strong>BỘ GIÁO DỤC VÀ ĐÀO TẠO</strong></p>");
        html.append("<p><strong>ĐẠI HỌC PHENIKAA</strong></p>");
        html.append("</div>");
        html.append("<div class='header-right'>");
        html.append("<p><strong>CỘNG HOÀ XÃ HỘI CHỦ NGHĨA VIỆT NAM</strong></p>");
        html.append("<p><strong>Độc lập - Tự do - Hạnh phúc</strong></p>");
        html.append("</div>");
        html.append("</div>");
        
        // Title
        html.append("<div class='title'>");
        html.append("<h1>PHIẾU ĐÁNH GIÁ ĐỒ ÁN/KHÓA LUẬN TỐT NGHIỆP CỦA THÀNH VIÊN HỘI ĐỒNG</h1>");
        html.append("</div>");
        
        // Section I: General Information
        html.append("<div class='section'>");
        html.append("<h2>I. THÔNG TIN CHUNG (HĐ 2)</h2>");
        html.append("<table class='info-table'>");
        html.append("<tr><td>Người đánh giá:</td><td>").append(request.getEvaluatorName()).append("</td></tr>");
        html.append("<tr><td>Đơn vị công tác:</td><td>").append(request.getEvaluatorDepartment()).append("</td></tr>");
        html.append("<tr><td>Học hàm, học vị:</td><td>").append(request.getEvaluatorTitle()).append("</td></tr>");
        html.append("<tr><td>Họ tên sinh viên:</td><td>").append(request.getStudentName()).append("</td></tr>");
        html.append("<tr><td>Mã SV:</td><td>").append(request.getStudentIdNumber()).append("</td></tr>");
        html.append("<tr><td>Ngành (CTĐT):</td><td>").append(request.getMajor()).append("</td></tr>");
        html.append("<tr><td>Tên đề tài:</td><td>").append(request.getTopicTitle()).append("</td></tr>");
        html.append("</table>");
        html.append("</div>");
        
        // Section II: Evaluation
        html.append("<div class='section'>");
        html.append("<h2>II. ĐÁNH GIÁ</h2>");
        html.append("<p class='note'>(Điểm từng tiêu chí và điểm cuối cùng làm tròn đến 1 chữ số thập phân)</p>");
        
        html.append("<table class='evaluation-table'>");
        html.append("<thead>");
        html.append("<tr>");
        html.append("<th>TT</th>");
        html.append("<th>Nội dung đánh giá</th>");
        html.append("<th>Điểm tối đa</th>");
        html.append("<th>Điểm đánh giá</th>");
        html.append("</tr>");
        html.append("</thead>");
        html.append("<tbody>");
        
        // Row 1: Trình bày nội dung
        html.append("<tr>");
        html.append("<td>1</td>");
        html.append("<td>Trình bày nội dung (slide rõ ràng, ngắn gọn, đầy đủ, đúng giờ)</td>");
        html.append("<td>0.5</td>");
        html.append("<td>").append(formatScore(request.getPresentationClarityScore())).append("</td>");
        html.append("</tr>");
        
        // Row 2: Trả lời câu hỏi GVPB
        html.append("<tr>");
        html.append("<td>2</td>");
        html.append("<td>Trả lời các câu hỏi của giảng viên phản biện</td>");
        html.append("<td>1.5</td>");
        html.append("<td>").append(formatScore(request.getReviewerQaScore())).append("</td>");
        html.append("</tr>");
        
        // Row 3: Trả lời câu hỏi hội đồng
        html.append("<tr>");
        html.append("<td>3</td>");
        html.append("<td>Trả lời các câu hỏi của thành viên hội đồng</td>");
        html.append("<td>1.5</td>");
        html.append("<td>").append(formatScore(request.getCommitteeQaScore())).append("</td>");
        html.append("</tr>");
        
        // Row 4: Tinh thần, thái độ
        html.append("<tr>");
        html.append("<td>4</td>");
        html.append("<td>Tinh thần, thái độ và cách ứng xử</td>");
        html.append("<td>1.0</td>");
        html.append("<td>").append(formatScore(request.getAttitudeScore())).append("</td>");
        html.append("</tr>");
        
        // Row 5: Thực hiện nội dung đề tài
        html.append("<tr>");
        html.append("<td>5</td>");
        html.append("<td>Thực hiện các nội dung của đề tài (về nội dung chuyên môn và khoa học cũng như về phương pháp nghiên cứu, xử lý vấn đề của ĐAKLTN có gì đúng, sai, có gì mới, mức độ sáng tạo)</td>");
        html.append("<td>4.5</td>");
        html.append("<td>").append(formatScore(request.getContentImplementationScore())).append("</td>");
        html.append("</tr>");
        
        // Row 6: Mối liên hệ vấn đề liên quan
        html.append("<tr>");
        html.append("<td>6</td>");
        html.append("<td>Mối liên hệ với những vấn đề liên quan (cơ sở lý thuyết và các hướng nghiên cứu khác có liên quan)</td>");
        html.append("<td>1.0</td>");
        html.append("<td>").append(formatScore(request.getRelatedIssuesScore())).append("</td>");
        html.append("</tr>");
        
        // Total row
        html.append("<tr class='total-row'>");
        html.append("<td colspan='2'><strong>Tổng số</strong></td>");
        html.append("<td><strong>10.0</strong></td>");
        html.append("<td><strong>").append(formatScore(request.getTotalScore())).append("</strong></td>");
        html.append("</tr>");
        
        html.append("</tbody>");
        html.append("</table>");
        html.append("</div>");
        
        // Section III: Comments
        html.append("<div class='section'>");
        html.append("<h2>III. NHẬN XÉT</h2>");
        html.append("<div class='comments-box'>");
        html.append("<p>").append(request.getComments() != null ? request.getComments() : "").append("</p>");
        html.append("</div>");
        html.append("</div>");
        
        // Q&A Section
        if (request.getQnaData() != null && !request.getQnaData().isEmpty()) {
            html.append("<div class='section'>");
            html.append("<h2>IV. TỔNG HỢP CÂU HỎI VÀ TRẢ LỜI</h2>");
            html.append("<div class='qna-section'>");
            
            for (int i = 0; i < request.getQnaData().size(); i++) {
                CommitteeEvaluationPDFRequest.QnAData qna = request.getQnaData().get(i);
                html.append("<div class='qna-item'>");
                html.append("<div class='question'>");
                html.append("<strong>Câu hỏi ").append(i + 1).append(":</strong> ");
                html.append("<span>").append(qna.getQuestion()).append("</span>");
                html.append("<span class='questioner'> - ").append(qna.getQuestionerName()).append("</span>");
                html.append("</div>");
                html.append("<div class='answer'>");
                html.append("<strong>Trả lời:</strong> ");
                html.append("<span>").append(qna.getAnswer() != null ? qna.getAnswer() : "Chưa trả lời").append("</span>");
                html.append("</div>");
                html.append("</div>");
            }
            
            html.append("</div>");
            html.append("</div>");
        }
        
        // Footer
        html.append("<div class='footer'>");
        html.append("<div class='signature-section'>");
        html.append("<p>Hà Nội, ngày ").append(request.getEvaluationDate().format(DateTimeFormatter.ofPattern("dd"))).append(" tháng ").append(request.getEvaluationDate().format(DateTimeFormatter.ofPattern("MM"))).append(" năm ").append(request.getEvaluationDate().format(DateTimeFormatter.ofPattern("yyyy"))).append("</p>");
        html.append("<p><strong>THÀNH VIÊN HỘI ĐỒNG</strong></p>");
        html.append("<p>(Ký, ghi rõ họ tên)</p>");
        html.append("<div class='signature-space'></div>");
        html.append("<p>").append(request.getEvaluatorName()).append("</p>");
        html.append("</div>");
        html.append("<div class='form-info'>");
        html.append("<p>BM.ĐT.19.16 (02-15/05/2025)-BL: 5 năm</p>");
        html.append("</div>");
        html.append("</div>");
        
        html.append("</body>");
        html.append("</html>");
        
        return html.toString();
        } catch (PDFGenerationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error generating HTML content: {}", e.getMessage(), e);
            throw new PDFGenerationException("Không thể tạo HTML content: " + e.getMessage(), e);
        }
    }
    
    /**
     * Chuyển đổi HTML sang PDF
     */
    private byte[] convertHTMLToPDF(String htmlContent) throws IOException {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            
            HtmlConverter.convertToPdf(htmlContent, outputStream);
            
            return outputStream.toByteArray();
        } catch (Exception e) {
            log.error("Error converting HTML to PDF: {}", e.getMessage(), e);
            throw new IOException("Không thể chuyển đổi HTML sang PDF: " + e.getMessage(), e);
        }
    }
    
    /**
     * CSS styles cho PDF
     */
    private String getCSSStyles() {
        return """
            <style>
                body { 
                    font-family: 'Times New Roman', serif; 
                    margin: 20px; 
                    line-height: 1.4; 
                    font-size: 12px;
                }
                
                .header { 
                    display: table; 
                    width: 100%;
                    margin-bottom: 20px; 
                    border-bottom: 2px solid #000; 
                    padding-bottom: 10px; 
                }
                
                .header-left, .header-right { 
                    display: table-cell;
                    width: 50%;
                    text-align: center; 
                }
                
                .header p { 
                    margin: 2px 0; 
                    font-size: 11px; 
                }
                
                .title { 
                    text-align: center; 
                    margin: 20px 0; 
                }
                
                .title h1 { 
                    font-size: 14px; 
                    font-weight: bold; 
                    margin: 0; 
                    text-transform: uppercase;
                }
                
                .section { 
                    margin: 20px 0; 
                }
                
                .section h2 { 
                    font-size: 13px; 
                    font-weight: bold; 
                    margin: 15px 0 10px 0; 
                    text-transform: uppercase;
                }
                
                .note { 
                    font-style: italic; 
                    font-size: 11px; 
                    margin: 5px 0; 
                }
                
                .info-table, .evaluation-table { 
                    width: 100%; 
                    border-collapse: collapse; 
                    margin: 10px 0; 
                    font-size: 11px;
                }
                
                .info-table td, .evaluation-table td, .evaluation-table th { 
                    padding: 6px; 
                    border: 1px solid #000; 
                    vertical-align: top;
                }
                
                .info-table td:first-child { 
                    background-color: #f5f5f5; 
                    font-weight: bold; 
                    width: 25%; 
                }
                
                .evaluation-table th { 
                    background-color: #e0e0e0; 
                    font-weight: bold; 
                    text-align: center;
                }
                
                .evaluation-table td:first-child { 
                    text-align: center; 
                    width: 5%; 
                }
                
                .evaluation-table td:nth-child(2) { 
                    width: 55%; 
                }
                
                .evaluation-table td:nth-child(3), 
                .evaluation-table td:nth-child(4) { 
                    text-align: center; 
                    width: 20%; 
                }
                
                .total-row { 
                    background-color: #f0f0f0; 
                    font-weight: bold; 
                }
                
                .comments-box { 
                    border: 1px solid #000; 
                    padding: 10px; 
                    min-height: 60px; 
                    margin: 10px 0; 
                }
                
                .qna-section { 
                    margin: 15px 0; 
                }
                
                .qna-item { 
                    margin: 15px 0; 
                    padding: 10px; 
                    border: 1px solid #ccc; 
                    border-radius: 5px; 
                }
                
                .question { 
                    margin-bottom: 8px; 
                    font-weight: bold; 
                }
                
                .questioner { 
                    font-style: italic; 
                    color: #666; 
                }
                
                .answer { 
                    margin-left: 20px; 
                }
                
                .footer { 
                    margin-top: 40px; 
                    display: table; 
                    width: 100%;
                }
                
                .signature-section { 
                    display: table-cell;
                    width: 70%;
                    text-align: right; 
                }
                
                .signature-section p { 
                    margin: 5px 0; 
                }
                
                .signature-space { 
                    height: 40px; 
                    margin: 20px 0; 
                }
                
                .form-info { 
                    display: table-cell;
                    width: 30%;
                    font-size: 10px; 
                    color: #666; 
                    vertical-align: bottom;
                }
            </style>
            """;
    }
    
    /**
     * Format điểm số
     */
    private String formatScore(Float score) {
        try {
            if (score == null) return "0.0";
            return String.format("%.1f", score);
        } catch (Exception e) {
            log.warn("Error formatting score: {}", e.getMessage());
            return "0.0";
        }
    }
}
