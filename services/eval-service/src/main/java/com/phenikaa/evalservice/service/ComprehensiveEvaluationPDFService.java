package com.phenikaa.evalservice.service;

import com.itextpdf.html2pdf.HtmlConverter;
import com.phenikaa.evalservice.client.ThesisServiceClient;
import com.phenikaa.evalservice.client.UserServiceClient;
import com.phenikaa.evalservice.dto.request.ComprehensiveEvaluationPDFRequest;
import com.phenikaa.evalservice.dto.response.QnAResponse;
import com.phenikaa.evalservice.entity.ProjectEvaluation;
import com.phenikaa.evalservice.exception.PDFGenerationException;
import com.phenikaa.evalservice.repository.ProjectEvaluationRepository;
import com.phenikaa.evalservice.service.DefenseQnAService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ComprehensiveEvaluationPDFService {
    
    private static final String STUDENT_PREFIX = "Sinh viên ";
    private static final String TOPIC_PREFIX = "Đề tài ";
    private static final String FULL_NAME_KEY = "fullName";
    private static final String MASTER_TITLE = "Thạc sĩ";
    private static final String DEPARTMENT_NAME = "Khoa Hệ thống thông tin";
    private static final String EVALUATION_NOTE = "(Điểm từng tiêu chí và điểm cuối cùng làm tròn đến 1 chữ số thập phân)";
    
    private final ProjectEvaluationRepository evaluationRepository;
    private final DefenseQnAService qnAService;
    private final ThesisServiceClient thesisServiceClient;
    private final UserServiceClient userServiceClient;
    
    /**
     * Tạo PDF tổng hợp đánh giá từ tất cả người chấm
     */
    public byte[] generateComprehensiveEvaluationPDF(ComprehensiveEvaluationPDFRequest request) {
        try {
            log.info("Generating comprehensive evaluation PDF for topic: {}, student: {}", 
                    request.getTopicId(), request.getStudentName());
            
            // Validate request
            if (request.getTopicId() == null) {
                throw new IllegalArgumentException("Topic ID cannot be null");
            }
            if (request.getStudentName() == null) {
                request.setStudentName("Sinh viên " + request.getTopicId());
            }
            
            String htmlContent = generateComprehensiveEvaluationHTML(request);
            log.info("Generated HTML content length: {}", htmlContent.length());
            
            byte[] pdfBytes = convertHTMLToPDF(htmlContent);
            log.info("Generated PDF with {} bytes", pdfBytes.length);
            
            return pdfBytes;
            
        } catch (PDFGenerationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error generating comprehensive evaluation PDF: {}", e.getMessage(), e);
            throw new PDFGenerationException("Không thể tạo PDF tổng hợp đánh giá: " + e.getMessage(), e);
        }
    }
    
    /**
     * Tạo PDF tổng hợp từ topic ID
     */
    public byte[] generateComprehensiveEvaluationPDFFromTopic(Integer topicId) {
        try {
            log.info("Generating comprehensive evaluation PDF for topic: {}", topicId);
            
            // Lấy tất cả đánh giá của topic
            List<ProjectEvaluation> evaluations = evaluationRepository
                    .findAllByTopicIdOrderByType(topicId);
            
            if (evaluations.isEmpty()) {
                throw new PDFGenerationException("Không tìm thấy đánh giá nào cho đề tài: " + topicId);
            }
            
            // Tạo request từ evaluations
            ComprehensiveEvaluationPDFRequest request = createRequestFromEvaluations(topicId, evaluations);
            
            return generateComprehensiveEvaluationPDF(request);
            
        } catch (Exception e) {
            log.error("Error generating comprehensive evaluation PDF for topic {}: {}", topicId, e.getMessage(), e);
            throw new PDFGenerationException("Không thể tạo PDF tổng hợp đánh giá: " + e.getMessage(), e);
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
            request.setStudentName(STUDENT_PREFIX + firstEvaluation.getStudentId());
            request.setStudentIdNumber("SV" + firstEvaluation.getStudentId());
            request.setClassName("K15-CNTT4");
            request.setMajor("Công nghệ thông tin");
            request.setBatch("K15");
            request.setTopicTitle(TOPIC_PREFIX + topicId);
            request.setEvaluationDate(firstEvaluation.getEvaluatedAt() != null ? firstEvaluation.getEvaluatedAt() : LocalDateTime.now());
            
            // Phân loại evaluations theo loại
            int committeeCount = 0;
            for (ProjectEvaluation evaluation : evaluations) {
                switch (evaluation.getEvaluationType()) {
                    case COMMITTEE:
                        committeeCount++;
                        if (committeeCount == 1) {
                            request.setChairman(createCommitteeMember(evaluation, "Thành viên hội đồng"));
                        } else if (committeeCount == 2) {
                            request.setSecretary(createCommitteeMember(evaluation, "Thành viên hội đồng"));
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
                    request.setTopicTitle(TOPIC_PREFIX + topicId);
                }
            } catch (Exception e) {
                log.warn("Error fetching topic info for topicId {}: {}", topicId, e.getMessage());
                request.setTopicTitle(TOPIC_PREFIX + topicId);
            }

            try {
                // Lấy thông tin sinh viên từ user-service
                Map<String, Object> userInfo = userServiceClient.getUserById(firstEvaluation.getStudentId());
                if (userInfo != null && !userInfo.isEmpty()) {
                    request.setStudentName((String) userInfo.get(FULL_NAME_KEY));
                    request.setStudentIdNumber("SV" + firstEvaluation.getStudentId());
                } else {
                    log.warn("Could not fetch user info for studentId: {}", firstEvaluation.getStudentId());
                    request.setStudentName(STUDENT_PREFIX + firstEvaluation.getStudentId());
                    request.setStudentIdNumber("SV" + firstEvaluation.getStudentId());
                }
            } catch (Exception e) {
                log.warn("Error fetching user info for studentId {}: {}", firstEvaluation.getStudentId(), e.getMessage());
                request.setStudentName(STUDENT_PREFIX + firstEvaluation.getStudentId());
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
        
        // Lấy thông tin giảng viên từ user-service
        try {
            Map<String, Object> evaluatorInfo = userServiceClient.getUserById(evaluation.getEvaluatorId());
            if (evaluatorInfo != null && !evaluatorInfo.isEmpty()) {
                member.setName((String) evaluatorInfo.get(FULL_NAME_KEY));
                member.setTitle(MASTER_TITLE); // Có thể lấy từ user info nếu có
                member.setDepartment(DEPARTMENT_NAME); // Có thể lấy từ user info nếu có
            } else {
                member.setName("ThS. " + role);
                member.setTitle(MASTER_TITLE);
                member.setDepartment(DEPARTMENT_NAME);
            }
        } catch (Exception e) {
            log.warn("Error fetching evaluator info for evaluatorId {}: {}", evaluation.getEvaluatorId(), e.getMessage());
            member.setName("ThS. " + role);
            member.setTitle(MASTER_TITLE);
            member.setDepartment(DEPARTMENT_NAME);
        }
        
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
        
        // Lấy thông tin giảng viên từ user-service
        try {
            Map<String, Object> evaluatorInfo = userServiceClient.getUserById(evaluation.getEvaluatorId());
            if (evaluatorInfo != null && !evaluatorInfo.isEmpty()) {
                reviewer.setName((String) evaluatorInfo.get("fullName"));
                reviewer.setTitle("Thạc sĩ"); // Có thể lấy từ user info nếu có
                reviewer.setDepartment("Khoa Hệ thống thông tin"); // Có thể lấy từ user info nếu có
            } else {
                reviewer.setName("ThS. Giảng viên phản biện");
                reviewer.setTitle("Thạc sĩ");
                reviewer.setDepartment("Khoa Hệ thống thông tin");
            }
        } catch (Exception e) {
            log.warn("Error fetching evaluator info for evaluatorId {}: {}", evaluation.getEvaluatorId(), e.getMessage());
            reviewer.setName("ThS. Giảng viên phản biện");
            reviewer.setTitle("Thạc sĩ");
            reviewer.setDepartment("Khoa Hệ thống thông tin");
        }
        
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
        
        // Lấy thông tin giảng viên từ user-service
        try {
            Map<String, Object> evaluatorInfo = userServiceClient.getUserById(evaluation.getEvaluatorId());
            if (evaluatorInfo != null && !evaluatorInfo.isEmpty()) {
                supervisor.setName((String) evaluatorInfo.get("fullName"));
                supervisor.setTitle("Thạc sĩ"); // Có thể lấy từ user info nếu có
                supervisor.setDepartment("Khoa Hệ thống thông tin"); // Có thể lấy từ user info nếu có
            } else {
                supervisor.setName("ThS. Giảng viên hướng dẫn");
                supervisor.setTitle("Thạc sĩ");
                supervisor.setDepartment("Khoa Hệ thống thông tin");
            }
        } catch (Exception e) {
            log.warn("Error fetching evaluator info for evaluatorId {}: {}", evaluation.getEvaluatorId(), e.getMessage());
            supervisor.setName("ThS. Giảng viên hướng dẫn");
            supervisor.setTitle("Thạc sĩ");
            supervisor.setDepartment("Khoa Hệ thống thông tin");
        }
        
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
     * Tạo HTML content cho PDF tổng hợp
     */
    private String generateComprehensiveEvaluationHTML(ComprehensiveEvaluationPDFRequest request) {
        try {
            StringBuilder html = new StringBuilder();
            
            html.append("<!DOCTYPE html>");
            html.append("<html>");
            html.append("<head>");
            html.append("<meta charset='UTF-8'>");
            html.append("<title>Báo cáo tổng hợp đánh giá đồ án/khóa luận tốt nghiệp</title>");
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
        html.append("<h1>BÁO CÁO TỔNG HỢP ĐÁNH GIÁ ĐỒ ÁN/KHÓA LUẬN TỐT NGHIỆP</h1>");
        html.append("</div>");
        
        // Section I: General Information
        html.append("<div class='section'>");
        html.append("<h2>I. THÔNG TIN CHUNG</h2>");
        html.append("<table class='info-table'>");
        html.append("<tr><td>Họ tên sinh viên:</td><td>").append(request.getStudentName()).append("</td></tr>");
        html.append("<tr><td>Mã SV:</td><td>").append(request.getStudentIdNumber()).append("</td></tr>");
        html.append("<tr><td>Lớp:</td><td>").append(request.getClassName()).append("</td></tr>");
        html.append("<tr><td>Ngành (CTĐT):</td><td>").append(request.getMajor()).append("</td></tr>");
        html.append("<tr><td>Khóa:</td><td>").append(request.getBatch()).append("</td></tr>");
        html.append("<tr><td>Tên đề tài:</td><td>").append(request.getTopicTitle()).append("</td></tr>");
        html.append("</table>");
        html.append("</div>");
        
        // Section II: Committee Evaluations
        if (request.getChairman() != null || request.getSecretary() != null || request.getMember() != null) {
            html.append("<div class='section'>");
            html.append("<h2>II. ĐÁNH GIÁ CỦA HỘI ĐỒNG CHẤM THI</h2>");
            html.append("<p class='note'>(Điểm từng tiêu chí và điểm cuối cùng làm tròn đến 1 chữ số thập phân)</p>");
            
            // Chairman evaluation
            if (request.getChairman() != null) {
                html.append("<div class='evaluator-section'>");
                html.append("<h3>1. Thành viên hội đồng: ").append(request.getChairman().getName()).append("</h3>");
                html.append(generateCommitteeEvaluationTable(request.getChairman()));
                html.append("</div>");
            }
            
            // Secretary evaluation
            if (request.getSecretary() != null) {
                html.append("<div class='evaluator-section'>");
                html.append("<h3>2. Thành viên hội đồng: ").append(request.getSecretary().getName()).append("</h3>");
                html.append(generateCommitteeEvaluationTable(request.getSecretary()));
                html.append("</div>");
            }
            
            // Member evaluation
            if (request.getMember() != null) {
                html.append("<div class='evaluator-section'>");
                html.append("<h3>3. Thành viên hội đồng: ").append(request.getMember().getName()).append("</h3>");
                html.append(generateCommitteeEvaluationTable(request.getMember()));
                html.append("</div>");
            }
            
            html.append("</div>");
        }
        
        // Section III: Reviewer Evaluation
        if (request.getReviewer() != null) {
            html.append("<div class='section'>");
            html.append("<h2>III. ĐÁNH GIÁ CỦA GIẢNG VIÊN PHẢN BIỆN</h2>");
            html.append("<p class='note'>(Điểm từng tiêu chí và điểm cuối cùng làm tròn đến 1 chữ số thập phân)</p>");
            html.append("<div class='evaluator-section'>");
            html.append("<h3>Giảng viên phản biện: ").append(request.getReviewer().getName()).append("</h3>");
            html.append(generateReviewerEvaluationTable(request.getReviewer()));
            html.append("</div>");
            html.append("</div>");
        }
        
        // Section IV: Supervisor Evaluation
        if (request.getSupervisor() != null) {
            html.append("<div class='section'>");
            html.append("<h2>IV. ĐÁNH GIÁ CỦA GIẢNG VIÊN HƯỚNG DẪN</h2>");
            html.append("<p class='note'>(Điểm từng tiêu chí và điểm cuối cùng làm tròn đến 1 chữ số thập phân)</p>");
            html.append("<div class='evaluator-section'>");
            html.append("<h3>Giảng viên hướng dẫn: ").append(request.getSupervisor().getName()).append("</h3>");
            html.append(generateSupervisorEvaluationTable(request.getSupervisor()));
            html.append("</div>");
            html.append("</div>");
        }
        
        // Section V: Q&A
        if (request.getQnaData() != null && !request.getQnaData().isEmpty()) {
            html.append("<div class='section'>");
            html.append("<h2>V. TỔNG HỢP CÂU HỎI VÀ TRẢ LỜI</h2>");
            html.append("<div class='qna-section'>");
            
            for (int i = 0; i < request.getQnaData().size(); i++) {
                ComprehensiveEvaluationPDFRequest.QnAData qna = request.getQnaData().get(i);
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
        html.append("<p><strong>TRƯỞNG KHOA</strong></p>");
        html.append("<p>(Ký, ghi rõ họ tên)</p>");
        html.append("<div class='signature-space'></div>");
        html.append("<p>PGS.TS. Nguyễn Văn A</p>");
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
     * Tạo bảng đánh giá hội đồng
     */
    private String generateCommitteeEvaluationTable(ComprehensiveEvaluationPDFRequest.CommitteeMember member) {
        StringBuilder table = new StringBuilder();
        
        table.append("<table class='evaluation-table'>");
        table.append("<thead>");
        table.append("<tr>");
        table.append("<th>TT</th>");
        table.append("<th>Nội dung đánh giá</th>");
        table.append("<th>Điểm tối đa</th>");
        table.append("<th>Điểm đánh giá</th>");
        table.append("</tr>");
        table.append("</thead>");
        table.append("<tbody>");
        
        // Row 1: Trình bày nội dung
        table.append("<tr>");
        table.append("<td>1</td>");
        table.append("<td>Trình bày nội dung (slide rõ ràng, ngắn gọn, đầy đủ, đúng giờ)</td>");
        table.append("<td>0.5</td>");
        table.append("<td>").append(formatScore(member.getPresentationClarityScore())).append("</td>");
        table.append("</tr>");
        
        // Row 2: Trả lời câu hỏi GVPB
        table.append("<tr>");
        table.append("<td>2</td>");
        table.append("<td>Trả lời các câu hỏi của giảng viên phản biện</td>");
        table.append("<td>1.5</td>");
        table.append("<td>").append(formatScore(member.getReviewerQaScore())).append("</td>");
        table.append("</tr>");
        
        // Row 3: Trả lời câu hỏi hội đồng
        table.append("<tr>");
        table.append("<td>3</td>");
        table.append("<td>Trả lời các câu hỏi của thành viên hội đồng</td>");
        table.append("<td>1.5</td>");
        table.append("<td>").append(formatScore(member.getCommitteeQaScore())).append("</td>");
        table.append("</tr>");
        
        // Row 4: Tinh thần, thái độ
        table.append("<tr>");
        table.append("<td>4</td>");
        table.append("<td>Tinh thần, thái độ và cách ứng xử</td>");
        table.append("<td>1.0</td>");
        table.append("<td>").append(formatScore(member.getAttitudeScore())).append("</td>");
        table.append("</tr>");
        
        // Row 5: Thực hiện nội dung đề tài
        table.append("<tr>");
        table.append("<td>5</td>");
        table.append("<td>Thực hiện các nội dung của đề tài (về nội dung chuyên môn và khoa học cũng như về phương pháp nghiên cứu, xử lý vấn đề của ĐAKLTN có gì đúng, sai, có gì mới, mức độ sáng tạo)</td>");
        table.append("<td>4.5</td>");
        table.append("<td>").append(formatScore(member.getContentImplementationScore())).append("</td>");
        table.append("</tr>");
        
        // Row 6: Mối liên hệ vấn đề liên quan
        table.append("<tr>");
        table.append("<td>6</td>");
        table.append("<td>Mối liên hệ với những vấn đề liên quan (cơ sở lý thuyết và các hướng nghiên cứu khác có liên quan)</td>");
        table.append("<td>1.0</td>");
        table.append("<td>").append(formatScore(member.getRelatedIssuesScore())).append("</td>");
        table.append("</tr>");
        
        // Total row
        table.append("<tr class='total-row'>");
        table.append("<td colspan='2'><strong>Tổng số</strong></td>");
        table.append("<td><strong>10.0</strong></td>");
        table.append("<td><strong>").append(formatScore(member.getTotalScore())).append("</strong></td>");
        table.append("</tr>");
        
        table.append("</tbody>");
        table.append("</table>");
        
        // Comments
        if (member.getComments() != null && !member.getComments().trim().isEmpty()) {
            table.append("<div class='comments-box'>");
            table.append("<p><strong>Nhận xét:</strong> ").append(member.getComments()).append("</p>");
            table.append("</div>");
        }
        
        return table.toString();
    }
    
    /**
     * Tạo bảng đánh giá phản biện
     */
    private String generateReviewerEvaluationTable(ComprehensiveEvaluationPDFRequest.Reviewer reviewer) {
        StringBuilder table = new StringBuilder();
        
        table.append("<table class='evaluation-table'>");
        table.append("<thead>");
        table.append("<tr>");
        table.append("<th>TT</th>");
        table.append("<th>Nội dung đánh giá</th>");
        table.append("<th>Điểm tối đa</th>");
        table.append("<th>Điểm đánh giá</th>");
        table.append("</tr>");
        table.append("</thead>");
        table.append("<tbody>");
        
        // Row 1: Hình thức trình bày
        table.append("<tr>");
        table.append("<td>1</td>");
        table.append("<td>Hình thức trình bày</td>");
        table.append("<td>1.5</td>");
        table.append("<td>").append(formatScore(reviewer.getPresentationFormatScore())).append("</td>");
        table.append("</tr>");
        
        // Row 2: Thực hiện nội dung đề tài
        table.append("<tr>");
        table.append("<td>2</td>");
        table.append("<td>Thực hiện nội dung đề tài</td>");
        table.append("<td>4.0</td>");
        table.append("<td>").append(formatScore(reviewer.getContentImplementationScore())).append("</td>");
        table.append("</tr>");
        
        // Row 3: Mối liên hệ vấn đề liên quan
        table.append("<tr>");
        table.append("<td>3</td>");
        table.append("<td>Mối liên hệ với những vấn đề liên quan</td>");
        table.append("<td>2.0</td>");
        table.append("<td>").append(formatScore(reviewer.getRelatedIssuesScore())).append("</td>");
        table.append("</tr>");
        
        // Row 4: Tính ứng dụng thực tiễn
        table.append("<tr>");
        table.append("<td>4</td>");
        table.append("<td>Tính ứng dụng thực tiễn</td>");
        table.append("<td>2.0</td>");
        table.append("<td>").append(formatScore(reviewer.getPracticalApplicationScore())).append("</td>");
        table.append("</tr>");
        
        // Row 5: Điểm thưởng
        table.append("<tr>");
        table.append("<td>5</td>");
        table.append("<td>Điểm thưởng</td>");
        table.append("<td>0.5</td>");
        table.append("<td>").append(formatScore(reviewer.getBonusScore())).append("</td>");
        table.append("</tr>");
        
        // Total row
        table.append("<tr class='total-row'>");
        table.append("<td colspan='2'><strong>Tổng số</strong></td>");
        table.append("<td><strong>10.0</strong></td>");
        table.append("<td><strong>").append(formatScore(reviewer.getTotalScore())).append("</strong></td>");
        table.append("</tr>");
        
        table.append("</tbody>");
        table.append("</table>");
        
        // Comments
        if (reviewer.getComments() != null && !reviewer.getComments().trim().isEmpty()) {
            table.append("<div class='comments-box'>");
            table.append("<p><strong>Nhận xét:</strong> ").append(reviewer.getComments()).append("</p>");
            table.append("</div>");
        }
        
        return table.toString();
    }
    
    /**
     * Tạo bảng đánh giá hướng dẫn
     */
    private String generateSupervisorEvaluationTable(ComprehensiveEvaluationPDFRequest.Supervisor supervisor) {
        StringBuilder table = new StringBuilder();
        
        table.append("<table class='evaluation-table'>");
        table.append("<thead>");
        table.append("<tr>");
        table.append("<th>TT</th>");
        table.append("<th>Nội dung đánh giá</th>");
        table.append("<th>Điểm tối đa</th>");
        table.append("<th>Điểm đánh giá</th>");
        table.append("</tr>");
        table.append("</thead>");
        table.append("<tbody>");
        
        // Row 1: Ý thức, thái độ sinh viên
        table.append("<tr>");
        table.append("<td>1</td>");
        table.append("<td>Ý thức, thái độ sinh viên</td>");
        table.append("<td>1.0</td>");
        table.append("<td>").append(formatScore(supervisor.getAttitudeScore())).append("</td>");
        table.append("</tr>");
        
        // Row 2: Khả năng xử lý vấn đề
        table.append("<tr>");
        table.append("<td>2</td>");
        table.append("<td>Khả năng xử lý vấn đề</td>");
        table.append("<td>1.0</td>");
        table.append("<td>").append(formatScore(supervisor.getProblemSolvingScore())).append("</td>");
        table.append("</tr>");
        
        // Row 3: Hình thức trình bày
        table.append("<tr>");
        table.append("<td>3</td>");
        table.append("<td>Hình thức trình bày</td>");
        table.append("<td>1.5</td>");
        table.append("<td>").append(formatScore(supervisor.getPresentationFormatScore())).append("</td>");
        table.append("</tr>");
        
        // Row 4: Thực hiện nội dung đề tài
        table.append("<tr>");
        table.append("<td>4</td>");
        table.append("<td>Thực hiện nội dung đề tài</td>");
        table.append("<td>4.5</td>");
        table.append("<td>").append(formatScore(supervisor.getContentImplementationScore())).append("</td>");
        table.append("</tr>");
        
        // Row 5: Mối liên hệ vấn đề liên quan
        table.append("<tr>");
        table.append("<td>5</td>");
        table.append("<td>Mối liên hệ với những vấn đề liên quan</td>");
        table.append("<td>1.0</td>");
        table.append("<td>").append(formatScore(supervisor.getRelatedIssuesScore())).append("</td>");
        table.append("</tr>");
        
        // Row 6: Tính ứng dụng thực tiễn
        table.append("<tr>");
        table.append("<td>6</td>");
        table.append("<td>Tính ứng dụng thực tiễn</td>");
        table.append("<td>1.0</td>");
        table.append("<td>").append(formatScore(supervisor.getPracticalApplicationScore())).append("</td>");
        table.append("</tr>");
        
        // Total row
        table.append("<tr class='total-row'>");
        table.append("<td colspan='2'><strong>Tổng số</strong></td>");
        table.append("<td><strong>10.0</strong></td>");
        table.append("<td><strong>").append(formatScore(supervisor.getTotalScore())).append("</strong></td>");
        table.append("</tr>");
        
        table.append("</tbody>");
        table.append("</table>");
        
        // Comments
        if (supervisor.getComments() != null && !supervisor.getComments().trim().isEmpty()) {
            table.append("<div class='comments-box'>");
            table.append("<p><strong>Nhận xét:</strong> ").append(supervisor.getComments()).append("</p>");
            table.append("</div>");
        }
        
        return table.toString();
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
                
                .section h3 { 
                    font-size: 12px; 
                    font-weight: bold; 
                    margin: 10px 0 5px 0; 
                }
                
                .evaluator-section {
                    margin: 15px 0;
                    page-break-inside: avoid;
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
                    min-height: 40px; 
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
