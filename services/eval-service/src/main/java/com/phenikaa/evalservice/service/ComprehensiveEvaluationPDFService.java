package com.phenikaa.evalservice.service;

import com.itextpdf.html2pdf.HtmlConverter;
import com.phenikaa.evalservice.client.ThesisServiceClient;
import com.phenikaa.evalservice.client.ProfileServiceClient;
import com.phenikaa.evalservice.client.UserServiceClient;
import com.phenikaa.evalservice.dto.request.ComprehensiveEvaluationPDFRequest;
import com.phenikaa.evalservice.dto.response.QnAResponse;
import com.phenikaa.evalservice.entity.ProjectEvaluation;
import com.phenikaa.evalservice.exception.PDFGenerationException;
import com.phenikaa.evalservice.repository.ProjectEvaluationRepository;
import com.phenikaa.evalservice.repository.StudentDefenseRepository;
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
    private static final String MASTER_TITLE = "";
    private static final String DEPARTMENT_NAME = "Khoa Hệ thống thông tin";
    private static final String EVALUATION_NOTE = "(Điểm từng tiêu chí và điểm cuối cùng làm tròn đến 1 chữ số thập phân)";
    
    private final ProjectEvaluationRepository evaluationRepository;
    private final DefenseQnAService qnAService;
    private final ThesisServiceClient thesisServiceClient;
    private final ProfileServiceClient profileServiceClient;
    private final StudentDefenseRepository studentDefenseRepository;
    private final CouncilSummaryService councilSummaryService;
    
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
            
            ProjectEvaluation firstEvaluation = evaluations.get(0);
            request.setStudentName(STUDENT_PREFIX + firstEvaluation.getStudentId());
            try {
                String code = null;
                Map<String, Object> studentProfileForCode = profileServiceClient.getStudentProfile(firstEvaluation.getStudentId());
                log.info("Fetched studentProfile keys for id {} => {}", firstEvaluation.getStudentId(), studentProfileForCode != null ? studentProfileForCode.keySet() : null);
                if (studentProfileForCode != null && !studentProfileForCode.isEmpty()) {
                    Object maybeUser = studentProfileForCode.get("user");
                    if (maybeUser == null) maybeUser = studentProfileForCode.get("data");
                    Map<?, ?> userMap = maybeUser instanceof Map<?, ?> m ? m : studentProfileForCode;
                    Object u = userMap.get("username");
                    if (u == null) u = userMap.get("email");
                    log.info("Fetched profile payload for id {} => username/email={}", firstEvaluation.getStudentId(), u);
                    if (u instanceof String us && !us.isBlank()) {
                        String prefix = us.contains("@") ? us.substring(0, us.indexOf('@')) : us;
                        code = prefix;
                    }
                }
                if (code == null || code.isBlank()) {
                    code = String.valueOf(firstEvaluation.getStudentId());
                }
                request.setStudentIdNumber(code);
                log.info("Resolved student code from profile for user {} => {}", firstEvaluation.getStudentId(), code);
            } catch (Exception ex) {
                String fallback = String.valueOf(firstEvaluation.getStudentId());
                request.setStudentIdNumber(fallback);
                log.warn("Failed to resolve student code from profile, fallback to id: {}", fallback);
            }
            request.setClassName("K15-CNTT4");
            request.setMajor("Công nghệ thông tin");
            request.setBatch("K15");
            request.setTopicTitle(TOPIC_PREFIX + topicId);
            request.setEvaluationDate(firstEvaluation.getEvaluatedAt() != null ? firstEvaluation.getEvaluatedAt() : LocalDateTime.now());
            
            // Gán địa điểm phòng từ lịch bảo vệ nếu có
            try {
                studentDefenseRepository.findWithSessionByTopicId(topicId).ifPresent(sd -> {
                    if (sd.getDefenseSession() != null && sd.getDefenseSession().getLocation() != null) {
                        request.setRoom(sd.getDefenseSession().getLocation());
                    }
                });
            } catch (Exception ex) {
                log.warn("Could not resolve defense room for topic {}: {}", topicId, ex.getMessage());
            }

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
                // Lấy thông tin sinh viên từ profile-service
                Map<String, Object> studentProfile = profileServiceClient.getStudentProfile(firstEvaluation.getStudentId());
                if (studentProfile != null && !studentProfile.isEmpty()) {
                    String fullName = (String) studentProfile.get("fullName");
                    if (fullName != null && !fullName.trim().isEmpty()) {
                        request.setStudentName(fullName);
                        // giữ nguyên mã SV lấy từ username ở trên nếu đã set
                        log.info("Successfully fetched student name: {} for studentId: {}", fullName, firstEvaluation.getStudentId());
                    } else {
                        log.warn("Full name is null or empty for studentId: {}", firstEvaluation.getStudentId());
                        request.setStudentName(STUDENT_PREFIX + firstEvaluation.getStudentId());
                    }
                    String className = (String) studentProfile.get("className");
                    if (className != null && !className.trim().isEmpty()) {
                        request.setClassName(className);
                    }
                    String major = (String) studentProfile.get("major");
                    if (major != null && !major.trim().isEmpty()) {
                        request.setMajor(major);
                    }
                } else {
                    log.warn("Could not fetch student profile for studentId: {}", firstEvaluation.getStudentId());
                    request.setStudentName(STUDENT_PREFIX + firstEvaluation.getStudentId());
                }
            } catch (Exception e) {
                log.warn("Error fetching student profile for studentId {}: {}", firstEvaluation.getStudentId(), e.getMessage());
                request.setStudentName(STUDENT_PREFIX + firstEvaluation.getStudentId());
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
        
        // Lấy thông tin giảng viên từ profile-service
        try {
            Map<String, Object> teacherProfile = profileServiceClient.getTeacherProfile(evaluation.getEvaluatorId());
            if (teacherProfile != null && !teacherProfile.isEmpty()) {
                String fullName = (String) teacherProfile.get("fullName");
                if (fullName != null && !fullName.trim().isEmpty()) {
                    member.setName(fullName);
                    String degree = (String) teacherProfile.get("degree");
                    member.setTitle(degree != null && !degree.trim().isEmpty() ? degree : MASTER_TITLE);
                    String department = (String) teacherProfile.get("department");
                    member.setDepartment(department != null ? department : DEPARTMENT_NAME);
                    log.info("Successfully fetched teacher name: {} for evaluatorId: {}", fullName, evaluation.getEvaluatorId());
                } else {
                    log.warn("Full name is null or empty for evaluatorId: {}", evaluation.getEvaluatorId());
                    member.setName("ThS. " + role);
                    member.setTitle(MASTER_TITLE);
                    member.setDepartment(DEPARTMENT_NAME);
                }
            } else {
                log.warn("Could not fetch teacher profile for evaluatorId: {}", evaluation.getEvaluatorId());
                member.setName("ThS. " + role);
                member.setTitle(MASTER_TITLE);
                member.setDepartment(DEPARTMENT_NAME);
            }
        } catch (Exception e) {
            log.warn("Error fetching teacher profile for evaluatorId {}: {}", evaluation.getEvaluatorId(), e.getMessage());
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
        
        // Lấy thông tin giảng viên từ profile-service
        try {
            Map<String, Object> teacherProfile = profileServiceClient.getTeacherProfile(evaluation.getEvaluatorId());
            if (teacherProfile != null && !teacherProfile.isEmpty()) {
                String fullName = (String) teacherProfile.get("fullName");
                if (fullName != null && !fullName.trim().isEmpty()) {
                reviewer.setName(fullName);
                String degree = (String) teacherProfile.get("degree");
                reviewer.setTitle(degree != null && !degree.trim().isEmpty() ? degree : MASTER_TITLE);
                String department = (String) teacherProfile.get("department");
                    reviewer.setDepartment(department != null ? department : DEPARTMENT_NAME);
                    log.info("Successfully fetched reviewer name: {} for evaluatorId: {}", fullName, evaluation.getEvaluatorId());
                } else {
                    log.warn("Full name is null or empty for evaluatorId: {}", evaluation.getEvaluatorId());
                    reviewer.setName("ThS. Giảng viên phản biện");
                    reviewer.setTitle(MASTER_TITLE);
                    reviewer.setDepartment(DEPARTMENT_NAME);
                }
            } else {
                log.warn("Could not fetch teacher profile for evaluatorId: {}", evaluation.getEvaluatorId());
                reviewer.setName("ThS. Giảng viên phản biện");
                reviewer.setTitle(MASTER_TITLE);
                reviewer.setDepartment(DEPARTMENT_NAME);
            }
        } catch (Exception e) {
            log.warn("Error fetching teacher profile for evaluatorId {}: {}", evaluation.getEvaluatorId(), e.getMessage());
            reviewer.setName("ThS. Giảng viên phản biện");
            reviewer.setTitle(MASTER_TITLE);
            reviewer.setDepartment(DEPARTMENT_NAME);
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
        
        // Lấy thông tin giảng viên từ profile-service
        try {
            Map<String, Object> teacherProfile = profileServiceClient.getTeacherProfile(evaluation.getEvaluatorId());
            if (teacherProfile != null && !teacherProfile.isEmpty()) {
                String fullName = (String) teacherProfile.get("fullName");
                if (fullName != null && !fullName.trim().isEmpty()) {
                supervisor.setName(fullName);
                String degree = (String) teacherProfile.get("degree");
                supervisor.setTitle(degree != null && !degree.trim().isEmpty() ? degree : MASTER_TITLE);
                String department = (String) teacherProfile.get("department");
                    supervisor.setDepartment(department != null ? department : DEPARTMENT_NAME);
                    log.info("Successfully fetched supervisor name: {} for evaluatorId: {}", fullName, evaluation.getEvaluatorId());
                } else {
                    log.warn("Full name is null or empty for evaluatorId: {}", evaluation.getEvaluatorId());
                    supervisor.setName("ThS. Giảng viên hướng dẫn");
                    supervisor.setTitle(MASTER_TITLE);
                    supervisor.setDepartment(DEPARTMENT_NAME);
                }
            } else {
                log.warn("Could not fetch teacher profile for evaluatorId: {}", evaluation.getEvaluatorId());
                supervisor.setName("ThS. Giảng viên hướng dẫn");
                supervisor.setTitle(MASTER_TITLE);
                supervisor.setDepartment(DEPARTMENT_NAME);
            }
        } catch (Exception e) {
            log.warn("Error fetching teacher profile for evaluatorId {}: {}", evaluation.getEvaluatorId(), e.getMessage());
            supervisor.setName("ThS. Giảng viên hướng dẫn");
            supervisor.setTitle(MASTER_TITLE);
            supervisor.setDepartment(DEPARTMENT_NAME);
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
     * Tạo HTML content cho PDF tổng hợp với 9 trang
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
        
            // ========== TRANG 1-2: BIÊN BẢN ĐÁNH GIÁ ==========
            html.append(generateMinutesPage(request));
            
            // ========== TRANG 3: PHIẾU ĐÁNH GIÁ HỘI ĐỒNG - CHỦ TỊCH ==========
            if (request.getChairman() != null) {
                html.append(generateCommitteeEvaluationFormPage(request.getChairman(), "CHỦ TỊCH HỘI ĐỒNG", request));
            }
            
            // ========== TRANG 4: PHIẾU ĐÁNH GIÁ HỘI ĐỒNG - THƯ KÝ ==========
            if (request.getSecretary() != null) {
                html.append(generateCommitteeEvaluationFormPage(request.getSecretary(), "THƯ KÝ", request));
            }
            
            // ========== TRANG 5: PHIẾU ĐÁNH GIÁ HỘI ĐỒNG - THÀNH VIÊN ==========
            if (request.getMember() != null) {
                html.append(generateCommitteeEvaluationFormPage(request.getMember(), "THÀNH VIÊN HỘI ĐỒNG", request));
            }
            
            // ========== TRANG 6: PHIẾU ĐÁNH GIÁ PHẢN BIỆN ==========
            if (request.getReviewer() != null) {
                html.append(generateReviewerEvaluationFormPage(request.getReviewer(), request));
            }
            
            // ========== TRANG 7: NHẬN XÉT PHẢN BIỆN ==========
            if (request.getReviewer() != null) {
                html.append(generateReviewerCommentsPage(request.getReviewer(), request));
            }
            
            // ========== TRANG 8: PHIẾU ĐÁNH GIÁ HƯỚNG DẪN ==========
            if (request.getSupervisor() != null) {
                html.append(generateSupervisorEvaluationFormPage(request.getSupervisor(), request));
            }
            
            // ========== TRANG 9: NHẬN XÉT HƯỚNG DẪN ==========
            if (request.getSupervisor() != null) {
                html.append(generateSupervisorCommentsPage(request.getSupervisor(), request));
            }
        
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
     * Tạo trang biên bản đánh giá (Trang 1-2)
     */
    private String generateMinutesPage(ComprehensiveEvaluationPDFRequest request) {
        StringBuilder html = new StringBuilder();
        
        // Page break
        html.append("<div class='page-break'></div>");
        
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
        html.append("<h1>BIÊN BẢN ĐÁNH GIÁ ĐỒ ÁN/KHÓA LUẬN TỐT NGHIỆP</h1>");
        html.append("</div>");
        
        // Section I: General Information (as list)
        html.append("<div class='section'>");
        html.append("<h2>I. Thông tin chung (HĐ 2)</h2>");
        html.append("<ol class='info-list'>");
        html.append("<li>Thời gian: <strong>từ </strong><strong><span>...</span> giờ <span>...</span></strong><strong> đến </strong><strong><span>...</span> giờ <span>...</span></strong><strong> ngày </strong><strong><span>.../.../20...</span></strong></li>");
        html.append("<li>Địa điểm: <strong>").append(request.getRoom()).append(", Đại học Phenikaa</strong></li>");
        html.append("<li>Họ tên sinh viên: <strong>").append(request.getStudentName()).append("</strong>&nbsp;&nbsp; Mã SV: <strong>").append(request.getStudentIdNumber() != null ? request.getStudentIdNumber() : "").append("</strong></li>");
        html.append("<li>Lớp: <strong>").append(request.getClassName() != null ? request.getClassName() : "").append("</strong>&nbsp;&nbsp; Ngành: <strong>").append(request.getMajor() != null ? request.getMajor() : "").append("</strong>&nbsp;&nbsp; Khóa: <strong>").append(request.getBatch() != null ? request.getBatch() : "").append("</strong></li>");
        html.append("<li>Tên đề tài: <strong><em>").append(request.getTopicTitle()).append("</em></strong></li>");
        html.append("<li>Giảng viên hướng dẫn: <strong>")
            .append(
                request.getSupervisor() != null
                    ? ((request.getSupervisor().getTitle() != null && !request.getSupervisor().getTitle().trim().isEmpty())
                        ? request.getSupervisor().getTitle() + " ."
                        : "")
                        + (request.getSupervisor().getName() != null ? request.getSupervisor().getName() : "")
                    : ""
            )
            .append("</strong></li>");
        html.append("</ol>");
        html.append("</div>");
        
        // Section II: Committee Members (as list)
        html.append("<div class='section'>");
        html.append("<h2>II. Thành phần</h2>");
        html.append("<ol class='member-list' style='margin-top:0; padding-left:20px;'>");
        if (request.getChairman() != null) {
            String chairmanName = (request.getChairman().getTitle() != null && !request.getChairman().getTitle().trim().isEmpty() ? request.getChairman().getTitle() + " ." : "")
                    + (request.getChairman().getName() != null ? request.getChairman().getName() : "");
            html.append("<li><span class='member-name'>").append(chairmanName).append("</span><span class='member-role'>Chủ tịch</span></li>");
        }
        if (request.getSecretary() != null) {
            String secretaryName = (request.getSecretary().getTitle() != null && !request.getSecretary().getTitle().trim().isEmpty() ? request.getSecretary().getTitle() + " ." : "")
                    + (request.getSecretary().getName() != null ? request.getSecretary().getName() : "");
            html.append("<li><span class='member-name'>").append(secretaryName).append("</span><span class='member-role'>Thư ký</span></li>");
        }
        if (request.getMember() != null) {
            String memberName = (request.getMember().getTitle() != null && !request.getMember().getTitle().trim().isEmpty() ? request.getMember().getTitle() + " ." : "")
                    + (request.getMember().getName() != null ? request.getMember().getName() : "");
            html.append("<li><span class='member-name'>").append(memberName).append("</span><span class='member-role'>Giảng viên phản biện</span></li>");
        }
        html.append("</ol>");
        html.append("</div>");
        
        // Section III: Q&A Summary (from backend DefenseQnA)
        html.append("<div class='section'>");
        html.append("<h2>III. Tổng hợp câu hỏi của thành viên hội đồng</h2>");
        if (request.getQnaData() != null && !request.getQnaData().isEmpty()) {
            html.append("<ol class='info-list'>");
            for (int i = 0; i < request.getQnaData().size(); i++) {
                var item = request.getQnaData().get(i);
                html.append("<li>")
                    .append(item.getQuestion() != null ? item.getQuestion() : "")
                    .append(item.getQuestionerName() != null ? " <em>(" + item.getQuestionerName() + ")</em>" : "")
                    .append("</li>");
            }
            html.append("</ol>");
        } else {
            html.append("<ol class='info-list'>");
            html.append("<li>................................................................................</li>");
            html.append("<li>................................................................................</li>");
            html.append("<li>................................................................................</li>");
            html.append("</ol>");
        }
        html.append("</div>");
        
        // Section IV: Student Answers (from backend DefenseQnA)
        html.append("<div class='section'>");
        html.append("<h2>IV. Tổng hợp nội dung trả lời của sinh viên</h2>");
        if (request.getQnaData() != null && !request.getQnaData().isEmpty()) {
            html.append("<ol class='info-list'>");
            for (int i = 0; i < request.getQnaData().size(); i++) {
                var item = request.getQnaData().get(i);
                String answer = item.getAnswer();
                html.append("<li>")
                    .append(answer != null && !answer.isBlank() ? answer : "................................................................................")
                    .append("</li>");
            }
            html.append("</ol>");
        } else {
            html.append("<ol class='info-list'>");
            html.append("<li>................................................................................</li>");
            html.append("<li>................................................................................</li>");
            html.append("<li>................................................................................</li>");
            html.append("</ol>");
        }
        html.append("</div>");
        
        // Page break for second page
        html.append("<div class='page-break'></div>");
        
        // Section V: Council Evaluation Content (from CouncilSummary, supports JSON content)
        html.append("<div class='section'>");
        html.append("<h2>V. Nội dung đánh giá của hội đồng</h2>");
        try {
            var summaryOpt = councilSummaryService.getByTopicId(request.getTopicId());
            if (summaryOpt.isPresent()) {
                String raw = String.valueOf(summaryOpt.get().getContent());
                if (raw != null && !raw.trim().isEmpty()) {
                    // Try parse JSON with 5 fields; fallback to raw html/text
                    try {
                        com.fasterxml.jackson.databind.ObjectMapper om = new com.fasterxml.jackson.databind.ObjectMapper();
                        java.util.Map<?,?> obj = om.readValue(raw, java.util.Map.class);
                        String meaning = getStringOr(obj, "meaning", getStringOr(obj, "significance", ""));
                        String structure = getStringOr(obj, "structure", "");
                        String methodology = getStringOr(obj, "methodology", "");
                        String results = getStringOr(obj, "results", "");
                        String prosCons = getStringOr(obj, "prosCons", "");

                        boolean any = !(isBlank(meaning) && isBlank(structure) && isBlank(methodology) && isBlank(results) && isBlank(prosCons));
                        html.append("<ol class='info-list'>");
                        html.append(renderCouncilItem("Ý nghĩa của đồ án/khóa luận:", meaning));
                        html.append(renderCouncilItem("Về nội dung, kết cấu của đồ án/khóa luận:", structure));
                        html.append(renderCouncilItem("Phương pháp nghiên cứu:", methodology));
                        html.append(renderCouncilItem("Các kết quả nghiên cứu đạt được:", results));
                        html.append(renderCouncilItem("Những ưu điểm, nhược điểm và nội dung cần bổ sung, chỉnh sửa:", prosCons));
                        html.append("</ol>");
                    } catch (Exception jsonEx) {
                        // Not a JSON; render raw as simple paragraph block
                        html.append("<div class='comments-box'>").append(safeText(raw)).append("</div>");
                    }
                } else {
                    html.append("<div class='comments-box'>");
                    html.append("...........................................................................................................................");
                    html.append("</div>");
                }
            } else {
                html.append("<div class='comments-box'>");
                html.append("...........................................................................................................................");
                html.append("</div>");
            }
        } catch (Exception ignore) {
            html.append("<div class='comments-box'>");
            html.append("...........................................................................................................................");
            html.append("</div>");
        }
        html.append("</div>");
        
        // Section VI: Final Score
        html.append("<div class='section'>");
        html.append("<h2>VI. ĐIỂM KẾT LUẬN</h2>");
        html.append("<p><strong>Điểm kết luận:</strong> <span class='underline'>9</span></p>");
        html.append("<p><strong>Bằng chữ:</strong> <span class='underline'>Chín phẩy</span></p>");
        html.append("</div>");
        
        // Signatures
        html.append("<div class='signature-section'>");
        html.append("<div class='signature-left'>");
        html.append("<p><strong>CHỦ TỊCH HỘI ĐỒNG</strong></p>");
        html.append("<p>(Ký, ghi rõ họ tên)</p>");
        html.append("<div class='signature-space'></div>");
        if (request.getChairman() != null) {
            html.append("<p>").append(request.getChairman().getName()).append("</p>");
        }
        html.append("</div>");
        html.append("<div class='signature-right'>");
        html.append("<p><strong>THƯ KÝ</strong></p>");
        html.append("<p>(Ký, ghi rõ họ tên)</p>");
        html.append("<div class='signature-space'></div>");
        if (request.getSecretary() != null) {
            html.append("<p>").append(request.getSecretary().getName()).append("</p>");
        }
        html.append("</div>");
        html.append("</div>");
        
        // Footer
        html.append("<div class='footer'>");
        html.append("<p>BM.ĐT.19.17 (02-15/05/2025)-BL: 5 năm</p>");
            html.append("</div>");
        
        return html.toString();
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static String getStringOr(java.util.Map<?,?> map, String key, String fallback) {
        if (map == null) return fallback;
        Object v = map.get(key);
        return v instanceof String str ? str : fallback;
    }

    private static String safeText(String text) {
        if (text == null) return "";
        // Minimal escaping of HTML special chars to avoid breaking layout
        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    private static String renderCouncilItem(String label, String value) {
        String content = isBlank(value)
                ? "................................................................................"
                : safeText(value);
        // Render as numbered item with label and value on a new line (no nested numbering inside)
        StringBuilder sb = new StringBuilder();
        sb.append("<li>");
        sb.append("<div><strong>").append(label).append("</strong></div>");
        sb.append("<div class='comments-box'>").append(content).append("</div>");
        sb.append("</li>");
        return sb.toString();
    }
    
    /**
     * Tạo trang phiếu đánh giá hội đồng
     */
    private String generateCommitteeEvaluationFormPage(ComprehensiveEvaluationPDFRequest.CommitteeMember member, String role, ComprehensiveEvaluationPDFRequest request) {
        StringBuilder html = new StringBuilder();
        
        // Page break
        html.append("<div class='page-break'></div>");
        
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
        html.append("<h1>PHIẾU ĐÁNH GIÁ ĐỒ ÁN/KHÓA LUẬN TỐT NGHIỆP</h1>");
        html.append("<h1>CỦA THÀNH VIÊN HỘI ĐỒNG</h1>");
        html.append("</div>");
        
        // Section I: General Information
            html.append("<div class='section'>");
        html.append("<h2>I. Thông tin chung (HĐ 2)</h2>");
        html.append("<table class='info-table'>");
        html.append("<tr><td>Người đánh giá:</td><td>").append(member.getName()).append("</td></tr>");
        html.append("<tr><td>Đơn vị công tác:</td><td>").append(member.getDepartment()).append("</td></tr>");
        html.append("<tr><td>Học hàm, học vị:</td><td>").append(member.getTitle()).append("</td></tr>");
        html.append("<tr><td>Họ tên sinh viên:</td><td>").append(request.getStudentName()).append("</td></tr>");
        html.append("<tr><td>Mã SV:</td><td>").append(request.getStudentIdNumber()).append("</td></tr>");
        html.append("<tr><td>Ngành (CTĐT):</td><td>").append(request.getMajor()).append("</td></tr>");
        html.append("<tr><td>Tên đề tài:</td><td>").append(request.getTopicTitle()).append("</td></tr>");
        html.append("</table>");
            html.append("</div>");
        
        // Section II: Evaluation
        html.append("<div class='section'>");
        html.append("<h2>II. ĐÁNH GIÁ</h2>");
        html.append("<p class='note'>").append(EVALUATION_NOTE).append("</p>");
        html.append(generateCommitteeEvaluationTable(member));
            html.append("</div>");
        
        // Section III: Comments
            html.append("<div class='section'>");
        html.append("<h2>III. NHẬN XÉT</h2>");
        html.append("<div class='comments-box'>");
        html.append("<p>").append(member.getComments() != null ? member.getComments() : "Đồng ý").append("</p>");
            html.append("</div>");
            html.append("</div>");
        
        // Footer
        html.append("<div class='footer'>");
        html.append("<div class='signature-section'>");
        html.append("<p>Hà Nội, ngày ").append(request.getEvaluationDate().format(DateTimeFormatter.ofPattern("dd"))).append(" tháng ").append(request.getEvaluationDate().format(DateTimeFormatter.ofPattern("MM"))).append(" năm ").append(request.getEvaluationDate().format(DateTimeFormatter.ofPattern("yyyy"))).append("</p>");
        html.append("<p><strong>").append(role).append("</strong></p>");
        html.append("<p>(Ký, ghi rõ họ tên)</p>");
        html.append("<div class='signature-space'></div>");
        html.append("<p>").append(member.getName()).append("</p>");
        html.append("</div>");
        html.append("<div class='form-info'>");
        html.append("<p>BM.ĐT.19.16 (02-15/05/2025)-BL: 5 năm</p>");
        html.append("</div>");
        html.append("</div>");
        
        return html.toString();
    }
    
    /**
     * Tạo trang phiếu đánh giá phản biện
     */
    private String generateReviewerEvaluationFormPage(ComprehensiveEvaluationPDFRequest.Reviewer reviewer, ComprehensiveEvaluationPDFRequest request) {
        StringBuilder html = new StringBuilder();
        
        // Page break
        html.append("<div class='page-break'></div>");
        
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
        html.append("<h1>PHIẾU ĐÁNH GIÁ ĐỒ ÁN/KHÓA LUẬN TỐT NGHIỆP</h1>");
        html.append("<h1>CỦA GIẢNG VIÊN PHẢN BIỆN</h1>");
        html.append("</div>");
        
        // Section I: General Information
            html.append("<div class='section'>");
        html.append("<h2>I. Thông tin chung</h2>");
        html.append("<table class='info-table'>");
        html.append("<tr><td>Người đánh giá:</td><td>").append(reviewer.getName()).append("</td></tr>");
        html.append("<tr><td>Học hàm, học vị:</td><td>").append(reviewer.getTitle()).append("</td></tr>");
        html.append("<tr><td>Đơn vị công tác:</td><td>").append(reviewer.getDepartment()).append("</td></tr>");
        html.append("<tr><td>Họ tên sinh viên:</td><td>").append(request.getStudentName()).append("</td></tr>");
        html.append("<tr><td>Mã SV:</td><td>").append(request.getStudentIdNumber()).append("</td></tr>");
        html.append("<tr><td>Ngành (CTĐT):</td><td>").append(request.getMajor()).append("</td></tr>");
        html.append("<tr><td>Tên đề tài:</td><td>").append(request.getTopicTitle()).append("</td></tr>");
        html.append("</table>");
        html.append("</div>");
        
        // Section II: Evaluation
        html.append("<div class='section'>");
        html.append("<h2>II. ĐÁNH GIÁ</h2>");
        html.append("<p class='note'>").append(EVALUATION_NOTE).append("</p>");
        html.append(generateReviewerEvaluationTable(reviewer));
                html.append("</div>");
        
        // Section III: Comments
        html.append("<div class='section'>");
        html.append("<h2>III. NHẬN XÉT</h2>");
        html.append("<div class='comments-box'>");
        html.append("<p>").append(reviewer.getComments() != null ? reviewer.getComments() : "Đồng ý").append("</p>");
                html.append("</div>");
                html.append("</div>");
        
        // Footer
        html.append("<div class='footer'>");
        html.append("<div class='signature-section'>");
        html.append("<p>Hà Nội, ngày ").append(request.getEvaluationDate().format(DateTimeFormatter.ofPattern("dd"))).append(" tháng ").append(request.getEvaluationDate().format(DateTimeFormatter.ofPattern("MM"))).append(" năm ").append(request.getEvaluationDate().format(DateTimeFormatter.ofPattern("yyyy"))).append("</p>");
        html.append("<p><strong>GIẢNG VIÊN PHẢN BIỆN</strong></p>");
        html.append("<p>(Ký, ghi rõ họ tên)</p>");
        html.append("<div class='signature-space'></div>");
        html.append("<p>").append(reviewer.getName()).append("</p>");
        html.append("</div>");
        html.append("<div class='form-info'>");
        html.append("<p>BM.ĐT.19.15 (02-15/05/2025)-BL: 5 năm</p>");
        html.append("</div>");
        html.append("</div>");
        
        return html.toString();
    }
    
    /**
     * Tạo trang nhận xét phản biện
     */
    private String generateReviewerCommentsPage(ComprehensiveEvaluationPDFRequest.Reviewer reviewer, ComprehensiveEvaluationPDFRequest request) {
        StringBuilder html = new StringBuilder();
        
        // Page break
        html.append("<div class='page-break'></div>");
        
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
        html.append("<h1>NHẬN XÉT ĐỒ ÁN/KHÓA LUẬN TỐT NGHIỆP</h1>");
        html.append("<h1>CỦA GIẢNG VIÊN PHẢN BIỆN</h1>");
        html.append("</div>");
        
        // Project Information
        html.append("<div class='section'>");
        html.append("<table class='info-table'>");
        html.append("<tr><td>Giảng viên phản biện:</td><td>").append(reviewer.getName()).append("</td></tr>");
        html.append("<tr><td>Bộ môn:</td><td>Khoa Hệ thống thông tin</td></tr>");
        html.append("<tr><td>Tên đề tài:</td><td>").append(request.getTopicTitle()).append("</td></tr>");
        html.append("<tr><td>Sinh viên thực hiện:</td><td>").append(request.getStudentName()).append("</td></tr>");
        html.append("<tr><td>Mã SV:</td><td>").append(request.getStudentIdNumber()).append("</td></tr>");
        html.append("<tr><td>Lớp:</td><td>").append(request.getClassName()).append("</td></tr>");
        html.append("<tr><td>Giảng viên hướng dẫn:</td><td>").append(request.getSupervisor() != null ? request.getSupervisor().getName() : "ThS. Nguyễn Thành Trung").append("</td></tr>");
        html.append("</table>");
        html.append("</div>");
        
        // Review Content
        html.append("<div class='section'>");
        html.append("<h2>NỘI DUNG NHẬN XÉT</h2>");
        html.append("<p><strong>1. Nhận xét ĐAKLTN:</strong></p>");
        html.append("<p><strong>- Bố cục, hình thức trình bày:</strong></p>");
        html.append("<p>Đồ án gồm 4 chương, nội dung rõ ràng, bố cục trình bày theo quy định của một khóa luận tốt nghiệp</p>");
        html.append("<p><strong>- Đảm bảo tính cấp thiết, hiện đại, không trùng lặp:</strong></p>");
        html.append("<p>Đồ án có tính cấp thiết nội dung phù hợp với hiện tại không trùng lặp với các đề tài khác.</p>");
        html.append("<p><strong>- Nội dung:</strong></p>");
        html.append("<p><strong>Nhận xét chung</strong></p>");
        html.append("<p><strong>Mục tiêu:</strong></p>");
        html.append("<p>Đồ án đặt ra mục tiêu xây dựng một hệ thống phần mềm quản lý toàn diện cho cửa hàng bánh sinh nhật, tối ưu quy trình đặt hàng trực tuyến và cung cấp giao diện quản trị trực quan cho người quản lý.</p>");
        html.append("<p><strong>Phạm vi:</strong></p>");
        html.append("<p>Hệ thống được triển khai dưới dạng ứng dụng web, phù hợp cho cửa hàng bánh quy mô nhỏ hoặc vừa. Dữ liệu được quản lý tập trung thông qua MongoDB.</p>");
        html.append("<p><strong>Đối tượng sử dụng:</strong></p>");
        html.append("<p>Hệ thống phục vụ hai nhóm đối tượng chính: Quản trị viên (Admin) và Khách hàng.</p>");
        html.append("<p><strong>Quản trị viên</strong> có các chức năng: quản lý sản phẩm, danh mục, đơn hàng, người dùng, mã giảm giá, bài viết và xem thống kê doanh thu.</p>");
        html.append("<p><strong>Khách hàng</strong> có thể: xem, tìm kiếm, đặt bánh, theo dõi đơn hàng, thanh toán trực tuyến hoặc khi nhận hàng, và quản lý thông tin cá nhân.</p>");
        html.append("<p><strong>Công nghệ sử dụng:</strong></p>");
        html.append("<p><strong>+ Frontend:</strong> Next.js kết hợp TypeScript và Tailwind CSS.</p>");
        html.append("<p><strong>+ Backend:</strong> NestJS (Node.js framework).</p>");
        html.append("<p><strong>+ Cơ sở dữ liệu:</strong> MongoDB.</p>");
        html.append("<p><strong>Bố cục báo cáo:</strong></p>");
        html.append("<p>Báo cáo đồ án được trình bày rõ ràng với 4 chương chính: Tổng quan đề tài, Phân tích thiết kế hệ thống, Phát triển và triển khai phần mềm, và Kiểm thử phần mềm.</p>");
        html.append("</div>");
        
        // Footer
        html.append("<div class='footer'>");
        html.append("<p>BM.DT.19.14 (02-15/05/2025)-BL: 5 năm</p>");
        html.append("</div>");
        
        return html.toString();
    }
    
    /**
     * Tạo trang phiếu đánh giá hướng dẫn
     */
    private String generateSupervisorEvaluationFormPage(ComprehensiveEvaluationPDFRequest.Supervisor supervisor, ComprehensiveEvaluationPDFRequest request) {
        StringBuilder html = new StringBuilder();
        
        // Page break
        html.append("<div class='page-break'></div>");
        
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
        html.append("<h1>PHIẾU ĐÁNH GIÁ ĐỒ ÁN/KHÓA LUẬN TỐT NGHIỆP</h1>");
        html.append("<h1>CỦA GIẢNG VIÊN HƯỚNG DẪN</h1>");
        html.append("</div>");
        
        // Section I: General Information
        html.append("<div class='section'>");
        html.append("<h2>I. Thông tin chung </h2>");
        html.append("<table class='info-table'>");
        html.append("<tr><td>Người đánh giá:</td><td>").append(supervisor.getName()).append("</td></tr>");
        html.append("<tr><td>Học hàm, học vị:</td><td>").append(supervisor.getTitle()).append("</td></tr>");
        html.append("<tr><td>Đơn vị công tác:</td><td>").append(supervisor.getDepartment()).append("</td></tr>");
        html.append("<tr><td>Họ tên sinh viên:</td><td>").append(request.getStudentName()).append("</td></tr>");
        html.append("<tr><td>Mã SV:</td><td>").append(request.getStudentIdNumber()).append("</td></tr>");
        html.append("<tr><td>Ngành:</td><td>").append(request.getMajor()).append("</td></tr>");
        html.append("<tr><td>Tên đề tài:</td><td>").append(request.getTopicTitle()).append("</td></tr>");
        html.append("</table>");
        html.append("</div>");
        
        // Section II: Evaluation
        html.append("<div class='section'>");
        html.append("<h2>II. ĐÁNH GIÁ</h2>");
        html.append("<p class='note'>").append(EVALUATION_NOTE).append("</p>");
        html.append(generateSupervisorEvaluationTable(supervisor));
        html.append("</div>");
        
        // Section III: Comments
        html.append("<div class='section'>");
        html.append("<h2>III. NHẬN XÉT</h2>");
        html.append("<div class='comments-box'>");
        html.append("<p>").append(supervisor.getComments() != null ? supervisor.getComments() : "Đồng ý").append("</p>");
        html.append("</div>");
        html.append("</div>");
        
        // Footer
        html.append("<div class='footer'>");
        html.append("<div class='signature-section'>");
        html.append("<p>Hà Nội, ngày ").append(request.getEvaluationDate().format(DateTimeFormatter.ofPattern("dd"))).append(" tháng ").append(request.getEvaluationDate().format(DateTimeFormatter.ofPattern("MM"))).append(" năm ").append(request.getEvaluationDate().format(DateTimeFormatter.ofPattern("yyyy"))).append("</p>");
        html.append("<p><strong>GIẢNG VIÊN HƯỚNG DẪN</strong></p>");
        html.append("<p>(Ký, ghi rõ họ tên)</p>");
        html.append("<div class='signature-space'></div>");
        html.append("<p>").append(supervisor.getName()).append("</p>");
        html.append("</div>");
        html.append("<div class='form-info'>");
        html.append("<p>BM.ĐT.19.23 (02-15/05/2025)-BL: 5 năm</p>");
        html.append("</div>");
        html.append("</div>");
        
        return html.toString();
    }
    
    /**
     * Tạo trang nhận xét hướng dẫn
     */
    private String generateSupervisorCommentsPage(ComprehensiveEvaluationPDFRequest.Supervisor supervisor, ComprehensiveEvaluationPDFRequest request) {
        StringBuilder html = new StringBuilder();
        
        // Page break
        html.append("<div class='page-break'></div>");
        
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
        html.append("<h1>NHẬN XÉT ĐỒ ÁN TỐT NGHIỆP CỦA GIẢNG VIÊN HƯỚNG DẪN</h1>");
        html.append("</div>");
        
        // Project Information
        html.append("<div class='section'>");
        html.append("<table class='info-table'>");
        html.append("<tr><td>Giảng viên hướng dẫn:</td><td>").append(supervisor.getName()).append("</td></tr>");
        html.append("<tr><td>Khoa:</td><td>Hệ thống thông tin</td></tr>");
        html.append("<tr><td>Tên đề tài:</td><td>").append(request.getTopicTitle()).append("</td></tr>");
        html.append("<tr><td>Sinh viên thực hiện:</td><td>").append(request.getStudentName()).append("</td></tr>");
        html.append("<tr><td>Lớp:</td><td>").append(request.getClassName()).append("</td></tr>");
        html.append("</table>");
        html.append("</div>");
        
        // Review Content
        html.append("<div class='section'>");
        html.append("<h2>NỘI DUNG NHẬN XÉT</h2>");
        html.append("<p><strong>I. Nhận xét ĐAKLTN (I. Review of Graduation Project):</strong></p>");
        html.append("<p>Nhận xét về hình thức: Báo cáo được trình bày khoa học, đúng quy định. Các mục được sắp xếp hợp lý, bảng biểu và hình ảnh minh họa rõ ràng; phần phụ lục cần ghi chi tiết thay vì để đường dẫn truy cập.</p>");
        html.append("<p>Tính cấp thiết của đề tài: Đề tài mang tính thực tiễn cao, với định hướng áp dụng tại một cơ sở kinh doanh cụ thể.</p>");
        html.append("<p>Mục tiêu của đề tài: Mục tiêu đề tài được xác định rõ ràng xây dựng một hệ thống quản lý cửa hàng, giúp cơ sở kinh doanh dễ dàng quản lý sản phẩm và doanh thu, tối ưu năng lực vận hành.</p>");
        html.append("<p>Nội dung của đề tài: Nội dung đề tài bám sát mục tiêu đề ra, bao gồm các phần: khảo sát thực trạng, phân tích yêu cầu, thiết kế hệ thống, triển khai và kiểm thử.</p>");
        html.append("<p>Tài liệu tham khảo: Có trích dẫn nhưng chưa đầy đủ, cần phải bổ sung.</p>");
        html.append("<p>Phương pháp nghiên cứu: Quy trình nghiên cứu đảm bảo tính khoa học và có sự kiểm thử để đánh giá kết quả.</p>");
        html.append("<p>Tính sáng tạo và ứng dụng: Hệ thống có tính ứng dụng cao, có thể triển khai thực tế để hỗ trợ cơ sở kinh doanh</p>");
        html.append("<p><strong>II. Nhận xét tinh thần và thái độ làm việc của sinh viên:</strong></p>");
        html.append("<p>Sinh viên có tinh thần chủ động trong việc nghiên cứu và phát triển hệ thống. Trong quá trình thực hiện đồ án, sinh viên đáp ứng đúng tiến độ và yêu cầu đặt ra.</p>");
        html.append("<p><strong>III. Kết quả đạt được:</strong></p>");
        html.append("<p>Hệ thống đã hoàn thiện các chức năng chính, bao gồm:</p>");
        html.append("<p>Quản lý tài khoản người dùng.</p>");
        html.append("<p>Quản lý thông tin doanh nghiệp và báo cáo tài chính.</p>");
        html.append("<p>Quản lý thông tin sản phẩm</p>");
        html.append("<p>Hệ thống hoạt động ổn định, giao diện trực quan, có tiềm năng tiếp tục phát triển để triển khai thực tế để hỗ trợ doanh nghiệp. Tuy nhiên, cần phải hoàn thiện và bổ sung thêm về các chức năng phân tích đa dạng hơn các dữ liệu.</p>");
        html.append("<p><strong>IV. Kết luận:</strong></p>");
        html.append("<p>☑ Đồng ý cho bảo vệ:</p>");
        html.append("<p>☐ Không đồng ý cho bảo vệ:</p>");
        html.append("</div>");
        
        // Footer
        html.append("<div class='footer'>");
        html.append("<div class='signature-section'>");
        html.append("<p>Hà Nội, ngày ").append(request.getEvaluationDate().format(DateTimeFormatter.ofPattern("dd"))).append(" tháng ").append(request.getEvaluationDate().format(DateTimeFormatter.ofPattern("MM"))).append(" năm ").append(request.getEvaluationDate().format(DateTimeFormatter.ofPattern("yyyy"))).append("</p>");
        html.append("<p><strong>GIẢNG VIÊN HƯỚNG DẪN</strong></p>");
        html.append("<p>(Ký, ghi rõ họ tên)</p>");
        html.append("<div class='signature-space'></div>");
        html.append("<p>").append(supervisor.getName()).append("</p>");
        html.append("</div>");
        html.append("<div class='form-info'>");
        html.append("<p>BM.DT.19.14 (02-15/05/2025)-BL: 5 năm</p>");
        html.append("</div>");
        html.append("</div>");
        
        return html.toString();
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
            
            // Ensure font embedding for consistent rendering
            com.itextpdf.html2pdf.ConverterProperties props = new com.itextpdf.html2pdf.ConverterProperties();
            com.itextpdf.layout.font.FontProvider fontProvider = new com.itextpdf.layout.font.FontProvider();
            try {
                fontProvider.addSystemFonts(); // load OS fonts (Windows/Linux/Mac)
                // Additionally try Windows fonts directory explicitly (no-op if not present)
                fontProvider.addDirectory("C:/Windows/Fonts");
            } catch (Exception ignore) { }
            props.setFontProvider(fontProvider);
            props.setCharset(java.nio.charset.StandardCharsets.UTF_8.name());

            HtmlConverter.convertToPdf(htmlContent, outputStream, props);
            
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
                    line-height: 1.5; 
                    font-size: 14px;
                }
                
                .page-break {
                    page-break-before: always;
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
                }
                
                .section { 
                    margin: 20px 0; 
                }
                
                .section h2 { 
                    font-size: 13px; 
                    font-weight: bold; 
                    margin: 15px 0 10px 0; 
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
                
                .underline {
                    text-decoration: underline;
                }
                
                .info-table, .evaluation-table { 
                    width: 100%; 
                    border-collapse: collapse; 
                    margin: 10px 0; 
                    font-size: 14px;
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

                .info-list {
                    margin: 10px 0 10px 20px;
                    padding-left: 18px;
                    list-style: decimal;
                    list-style-position: outside;
                    font-size: 14px;
                }
                .info-list li { 
                    margin: 4px 0; 
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
                    border: none; 
                    padding: 6px 2px; 
                    min-height: 40px; 
                    margin: 8px 0; 
                    /* Dotted guideline lines under text */
                    background-image: repeating-linear-gradient(
                        to bottom,
                        transparent 0,
                        transparent 18px,
                        #000 18px,
                        #000 19px
                    );
                    line-height: 18px; 
                }

                .member-list { 
                    margin: 10px 0 10px 20px; 
                    padding-left: 20px; 
                    list-style: decimal; 
                    list-style-position: outside; 
                    font-size: 14px; 
                }
                .member-list li { 
                    /* keep default list-item to preserve numbering */
                    margin: 4px 0; 
                }
                .member-name { 
                    display: inline-block; 
                    width: 60%; 
                }
                .member-role { 
                    display: inline-block; 
                    width: 35%; 
                    text-align: left; 
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
                
                .signature-left, .signature-right {
                    display: table-cell;
                    width: 50%;
                    text-align: center;
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
