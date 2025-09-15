package com.phenikaa.evalservice.service;

import com.phenikaa.evalservice.dto.request.EvaluationRequest;
import com.phenikaa.evalservice.dto.response.EvaluationResponse;
import com.phenikaa.evalservice.dto.response.FinalScoreResponse;
import com.phenikaa.evalservice.entity.ProjectEvaluation;
import com.phenikaa.evalservice.entity.DefenseCommittee;
import com.phenikaa.evalservice.repository.ProjectEvaluationRepository;
import com.phenikaa.evalservice.repository.StudentDefenseRepository;
import com.phenikaa.evalservice.repository.DefenseCommitteeRepository;
import com.phenikaa.evalservice.repository.DefenseSessionRepository;
import com.phenikaa.evalservice.client.ThesisServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EvaluationService {
    
    private final ProjectEvaluationRepository evaluationRepository;
    private final StudentDefenseRepository studentDefenseRepository;
    private final DefenseCommitteeRepository defenseCommitteeRepository;
    private final DefenseSessionRepository defenseSessionRepository;
    private final ThesisServiceClient thesisServiceClient;
    
    /**
     * Chấm điểm cho sinh viên
     */
    public EvaluationResponse submitEvaluation(EvaluationRequest request) {
        log.info("Submitting evaluation for topic: {}, student: {}, evaluator: {}, type: {}", 
                request.getTopicId(), request.getStudentId(), request.getEvaluatorId(), request.getEvaluationType());
        
        // 0) Ủy quyền theo vai trò: chỉ cho phép khi evaluator có nhiệm vụ hợp lệ
        if (!isEvaluatorAuthorizedFor(request.getEvaluatorId(), request.getTopicId(), request.getEvaluationType())) {
            throw new RuntimeException("Evaluator is not authorized to submit this evaluation");
        }

        // 1) Validate scores based on role-specific criteria
        validateScoresByRole(request);

        // Kiểm tra xem đã có đánh giá chưa (lấy bản ghi mới nhất nếu có nhiều)
        var existingEvaluations = evaluationRepository
                .findAllByTopicIdAndEvaluatorIdAndEvaluationTypeOrderByEvaluatedAtDesc(
                        request.getTopicId(),
                        request.getEvaluatorId(),
                        request.getEvaluationType()
                );

        ProjectEvaluation evaluation;
        if (!existingEvaluations.isEmpty()) {
            // Cập nhật đánh giá hiện có (bản ghi mới nhất)
            evaluation = existingEvaluations.get(0);
            log.info("Updating existing evaluation: {}", evaluation.getEvaluationId());
        } else {
            // Tạo đánh giá mới
            evaluation = new ProjectEvaluation();
            evaluation.setTopicId(request.getTopicId());
            evaluation.setStudentId(request.getStudentId());
            evaluation.setEvaluatorId(request.getEvaluatorId());
            evaluation.setEvaluationType(request.getEvaluationType());
            evaluation.setEvaluationStatus(ProjectEvaluation.EvaluationStatus.IN_PROGRESS);
        }
        
        // Cập nhật điểm số theo vai trò
        mapScoresByRole(request, evaluation);
        evaluation.setComments(request.getComments());
        
        // Tính tổng điểm
        evaluation.calculateTotalScore();
        
        // Cập nhật trạng thái và thời gian
        evaluation.setEvaluationStatus(ProjectEvaluation.EvaluationStatus.COMPLETED);
        evaluation.setEvaluatedAt(LocalDateTime.now());
        
        // Lưu vào database
        ProjectEvaluation savedEvaluation = evaluationRepository.save(evaluation);
        
        log.info("Evaluation submitted successfully: {}", savedEvaluation.getEvaluationId());
        
        return convertToResponse(savedEvaluation);
    }

    private boolean isEvaluatorAuthorizedFor(Integer evaluatorId, Integer topicId, ProjectEvaluation.EvaluationType type) {
        // Tìm assignment theo topicId
        var sdOpt = studentDefenseRepository.findByTopicId(topicId);
        if (sdOpt.isEmpty()) {
            log.warn("Authorization failed: no StudentDefense for topic {}", topicId);
            return false;
        }
        var sd = sdOpt.get();

        // GVHD
        if (type == ProjectEvaluation.EvaluationType.SUPERVISOR) {
            return sd.getSupervisorId() != null && sd.getSupervisorId().equals(evaluatorId);
        }

        // Reviewer/Committee: kiểm tra giảng viên thuộc hội đồng của session
        var session = sd.getDefenseSession();
        if (session == null) {
            log.warn("Authorization failed: no defense session for topic {}", topicId);
            return false;
        }

        var committees = defenseCommitteeRepository.findByDefenseSession_SessionId(session.getSessionId());
        boolean inCommittee = committees.stream().anyMatch(dc -> dc.getLecturerId() != null && dc.getLecturerId().equals(evaluatorId));

        if (!inCommittee) return false;

        if (type == ProjectEvaluation.EvaluationType.REVIEWER) {
            return committees.stream().anyMatch(dc -> dc.getLecturerId() != null &&
                    dc.getLecturerId().equals(evaluatorId) && dc.getRole() == DefenseCommittee.CommitteeRole.REVIEWER);
        }

        // COMMITTEE: bất kỳ vai trò nào trong hội đồng
        return true;
    }
    
    /**
     * Lấy tất cả đánh giá của một topic
     */
    public List<EvaluationResponse> getEvaluationsByTopic(Integer topicId) {
        List<ProjectEvaluation> evaluations = evaluationRepository.findAllByTopicIdOrderByType(topicId);
        return evaluations.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Lấy đánh giá theo sinh viên
     */
    public List<EvaluationResponse> getEvaluationsByStudent(Integer studentId) {
        List<ProjectEvaluation> evaluations = evaluationRepository.findByStudentId(studentId);
        return evaluations.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Lấy đánh giá theo giảng viên
     */
    public List<EvaluationResponse> getEvaluationsByEvaluator(Integer evaluatorId) {
        List<ProjectEvaluation> evaluations = evaluationRepository.findByEvaluatorId(evaluatorId);
        return evaluations.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lấy danh sách nhiệm vụ chấm điểm theo giảng viên
     * - Hiển thị tất cả đề tài mà sinh viên đã được gán vào buổi bảo vệ
     * - GVHD: từ StudentDefense.supervisorId = evaluatorId
     * - Hội đồng: giảng viên là thành viên DefenseCommittee của session → lấy toàn bộ StudentDefense của session đó
     * - Nếu đã có ProjectEvaluation tương ứng thì đánh dấu COMPLETED, ngược lại PENDING
     */
    public List<EvaluationResponse> getEvaluatorTasks(Integer evaluatorId, LocalDate date, String scope) {
        log.info("Getting evaluator tasks for evaluatorId={}, date={}, scope={}", evaluatorId, date, scope);

        // Tạo StudentDefense mẫu nếu chưa có
        createSampleStudentDefenses();

        // Lấy tất cả student defenses với DefenseSession được fetch
        var allStudentDefenses = studentDefenseRepository.findAllWithDefenseSession();
        log.info("Total student defenses in database: {}", allStudentDefenses.size());
        
        // Debug: Log chi tiết từng student defense
        for (var sd : allStudentDefenses) {
            log.info("StudentDefense ID: {}, Student ID: {}, Topic ID: {}, Supervisor ID: {}, Session: {}, DefenseDate: {}", 
                sd.getStudentDefenseId(),
                sd.getStudentId(),
                sd.getTopicId(),
                sd.getSupervisorId(),
                sd.getDefenseSession() != null ? sd.getDefenseSession().getSessionId() : "NULL",
                sd.getDefenseSession() != null ? sd.getDefenseSession().getDefenseDate() : "NULL");
        }

        // 1) GVHD: sinh viên do giảng viên này hướng dẫn
        var supervisorAssignments = allStudentDefenses.stream()
                .filter(sd -> sd.getSupervisorId() != null && sd.getSupervisorId().equals(evaluatorId))
                .filter(sd -> sd.getDefenseSession() != null)
                .collect(Collectors.toList());
        log.info("Role-based: {} supervisor assignments for evaluatorId={}", supervisorAssignments.size(), evaluatorId);
        
        // Debug: Log chi tiết supervisor assignments
        for (var sa : supervisorAssignments) {
            log.info("DEBUG: Supervisor assignment topicId={}, studentId={}, supervisorId={}, sessionId={}", 
                sa.getTopicId(), sa.getStudentId(), sa.getSupervisorId(), 
                sa.getDefenseSession() != null ? sa.getDefenseSession().getSessionId() : "NULL");
        }

        // 2) Thành viên hội đồng: lấy các session mà giảng viên thuộc hội đồng
        var allCommittees = defenseCommitteeRepository.findByLecturerId(evaluatorId);
        log.info("DEBUG: All committees for evaluatorId={}: {}", evaluatorId, allCommittees.stream()
                .map(dc -> "sessionId=" + (dc.getDefenseSession() != null ? dc.getDefenseSession().getSessionId() : "NULL") + 
                           ", role=" + dc.getRole())
                .collect(Collectors.toList()));
        
        var committeeSessions = allCommittees.stream()
                .map(DefenseCommittee::getDefenseSession)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());
        log.info("Role-based: {} committee sessions for evaluatorId={}", committeeSessions.size(), evaluatorId);

        log.info("DEBUG: All student defenses: {}", allStudentDefenses.stream()
                .map(sd -> "topicId=" + sd.getTopicId() + 
                           ", sessionId=" + (sd.getDefenseSession() != null ? sd.getDefenseSession().getSessionId() : "NULL"))
                .collect(Collectors.toList()));
        
        var committeeAssignments = allStudentDefenses.stream()
                .filter(sd -> sd.getDefenseSession() != null &&
                        committeeSessions.stream().anyMatch(cs -> cs.getSessionId().equals(sd.getDefenseSession().getSessionId())))
                .collect(Collectors.toList());
        log.info("Role-based: {} committee/reviewer assignments for evaluatorId={}", committeeAssignments.size(), evaluatorId);

        // 3. Tạo tasks cho từng loại
        var supervisorTasks = supervisorAssignments.stream()
                .map(sd -> buildTaskFromStudentDefense(sd, evaluatorId, ProjectEvaluation.EvaluationType.SUPERVISOR))
                .collect(Collectors.toList());

        var committeeTasks = new ArrayList<EvaluationResponse>();
        var reviewerTasks = new ArrayList<EvaluationResponse>();
        
        log.info("DEBUG: Creating tasks for {} assignments", committeeAssignments.size());
        
        for (var assignment : committeeAssignments) {
            // Lấy TẤT CẢ roles của evaluator trong session này
            var rolesInSession = allCommittees.stream()
                    .filter(dc -> dc.getDefenseSession() != null &&
                            dc.getDefenseSession().getSessionId().equals(assignment.getDefenseSession().getSessionId()) &&
                            dc.getLecturerId().equals(evaluatorId))
                    .map(DefenseCommittee::getRole)
                    .collect(Collectors.toSet());

            log.info("DEBUG: Assignment topicId={}, sessionId={}, roles={}, evaluatorId={}", 
                assignment.getTopicId(), 
                assignment.getDefenseSession().getSessionId(), 
                rolesInSession, 
                evaluatorId);

            // Tạo task cho từng role
            for (var role : rolesInSession) {
                if (role == DefenseCommittee.CommitteeRole.REVIEWER) {
                    log.info("DEBUG: Creating REVIEWER task for topicId={}", assignment.getTopicId());
                    reviewerTasks.add(buildTaskFromStudentDefense(assignment, evaluatorId, ProjectEvaluation.EvaluationType.REVIEWER));
                } else if (role == DefenseCommittee.CommitteeRole.MEMBER ||
                           role == DefenseCommittee.CommitteeRole.CHAIRMAN ||
                           role == DefenseCommittee.CommitteeRole.SECRETARY) {
                    log.info("DEBUG: Creating COMMITTEE task for topicId={}", assignment.getTopicId());
                    committeeTasks.add(buildTaskFromStudentDefense(assignment, evaluatorId, ProjectEvaluation.EvaluationType.COMMITTEE));
                }
            }
        }

        // 4. Gộp tất cả tasks và loại trùng (topicId + evaluationType)
        var all = new java.util.LinkedHashMap<String, EvaluationResponse>();
        java.util.function.Consumer<EvaluationResponse> put = er -> {
            String key = er.getTopicId() + ":" + er.getEvaluationType();
            all.putIfAbsent(key, er);
        };
        
        log.info("DEBUG: Before deduplication - supervisorTasks={}, committeeTasks={}, reviewerTasks={}", 
            supervisorTasks.size(), committeeTasks.size(), reviewerTasks.size());
        
        supervisorTasks.forEach(put);
        committeeTasks.forEach(put);
        reviewerTasks.forEach(put);

        var result = all.values().stream().collect(Collectors.toList());
        log.info("Returning {} total tasks for evaluatorId={}", result.size(), evaluatorId);
        
        // Debug: Log final tasks
        for (var task : result) {
            log.info("DEBUG: Final task topicId={}, evaluationType={}, studentId={}", 
                task.getTopicId(), task.getEvaluationType(), task.getStudentId());
        }
        
        return result;
    }

    // Overload để tương thích khi không truyền scope (mặc định today)
    public List<EvaluationResponse> getEvaluatorTasks(Integer evaluatorId, LocalDate date) {
        return getEvaluatorTasks(evaluatorId, date, "today");
    }

    // Debug methods
    public List<com.phenikaa.evalservice.entity.StudentDefense> getAllStudentDefenses() {
        return studentDefenseRepository.findAll();
    }

    public List<com.phenikaa.evalservice.entity.DefenseSession> getAllDefenseSessions() {
        return defenseSessionRepository.findAll();
    }

    public List<com.phenikaa.evalservice.entity.DefenseCommittee> getAllDefenseCommittees() {
        return defenseCommitteeRepository.findAll();
    }
    
    // Method để tạo StudentDefense mẫu dựa trên DefenseCommittee
    public void createSampleStudentDefenses() {
        log.info("=== CREATING SAMPLE STUDENT DEFENSES ===");
        
        // Kiểm tra xem đã có StudentDefense chưa
        var existingDefenses = studentDefenseRepository.findAll();
        if (!existingDefenses.isEmpty()) {
            log.info("StudentDefenses already exist, skipping creation");
            return;
        }
        
        // Lấy tất cả DefenseSession
        var sessions = defenseSessionRepository.findAll();
        if (sessions.isEmpty()) {
            log.warn("No DefenseSessions found, cannot create StudentDefenses");
            return;
        }
        
        // Tạo StudentDefense cho mỗi session
        for (var session : sessions) {
            // Tạo 2-3 sinh viên mẫu cho mỗi session
            for (int i = 1; i <= 3; i++) {
                var studentDefense = com.phenikaa.evalservice.entity.StudentDefense.builder()
                        .defenseSession(session)
                        .studentId(100 + i) // ID sinh viên mẫu
                        .topicId(1 + i) // ID đề tài mẫu
                        .supervisorId(109) // Giảng viên hướng dẫn mẫu
                        .studentName("Sinh viên " + i)
                        .studentMajor("Công nghệ thông tin")
                        .topicTitle("Đề tài mẫu " + i)
                        .defenseOrder(i)
                        .defenseTime(session.getStartTime())
                        .durationMinutes(60)
                        .status(com.phenikaa.evalservice.entity.StudentDefense.DefenseStatus.SCHEDULED)
                        .build();
                
                studentDefenseRepository.save(studentDefense);
                log.info("Created StudentDefense for session {} with student {}", 
                    session.getSessionId(), studentDefense.getStudentId());
            }
        }
        
        log.info("=== END CREATING SAMPLE STUDENT DEFENSES ===");
    }
    
    public com.phenikaa.evalservice.entity.StudentDefense saveStudentDefense(com.phenikaa.evalservice.entity.StudentDefense studentDefense) {
        return studentDefenseRepository.save(studentDefense);
    }
    
    public java.util.Optional<com.phenikaa.evalservice.entity.DefenseCommittee> getDefenseCommitteeById(Integer committeeId) {
        return defenseCommitteeRepository.findById(committeeId);
    }
    
    public com.phenikaa.evalservice.entity.DefenseCommittee confirmDefenseCommittee(Integer committeeId) {
        var committee = defenseCommitteeRepository.findById(committeeId)
                .orElseThrow(() -> new RuntimeException("Defense committee not found"));
        
        committee.setStatus(com.phenikaa.evalservice.entity.DefenseCommittee.CommitteeStatus.CONFIRMED);
        committee.setRespondedAt(java.time.LocalDateTime.now());
        
        return defenseCommitteeRepository.save(committee);
    }

    private EvaluationResponse buildTaskFromStudentDefense(
            com.phenikaa.evalservice.entity.StudentDefense sd,
            Integer evaluatorId,
            ProjectEvaluation.EvaluationType type
    ) {
        // Kiểm tra đã có bản ghi chấm chưa (lấy bản ghi mới nhất nếu có)
        var existingList = evaluationRepository
                .findAllByTopicIdAndEvaluatorIdAndEvaluationTypeOrderByEvaluatedAtDesc(
                        sd.getTopicId(), evaluatorId, type
                );

        if (!existingList.isEmpty()) {
            // Trả về response của bản ghi đã có
            return convertToResponse(existingList.get(0));
        }

        // Tạo một EvaluationResponse ở trạng thái nhiệm vụ PENDING
        EvaluationResponse resp = new EvaluationResponse();
        resp.setEvaluationId(null);
        resp.setTopicId(sd.getTopicId());
        resp.setStudentId(sd.getStudentId());
        resp.setEvaluatorId(evaluatorId);
        resp.setEvaluationType(type);
        resp.setEvaluationStatus(ProjectEvaluation.EvaluationStatus.PENDING);
        
        // Lấy thông tin từ StudentDefense trước
        resp.setStudentName(sd.getStudentName());
        resp.setTopicTitle(sd.getTopicTitle());
        
        // Cố gắng lấy thông tin đầy đủ từ thesis-service
        try {
            var topicInfo = thesisServiceClient.getTopicById(sd.getTopicId());
            if (topicInfo != null && topicInfo.get("title") != null) {
                resp.setTopicTitle(topicInfo.get("title").toString());
            }
        } catch (Exception e) {
            log.warn("Could not fetch topic info for topicId {}: {}", sd.getTopicId(), e.getMessage());
            // Fallback to existing data
        }
        
        // Set defense date and time from defense session
        if (sd.getDefenseSession() != null) {
            resp.setDefenseDate(sd.getDefenseSession().getDefenseDate());
            resp.setDefenseTime(sd.getDefenseSession().getStartTime());
        }
        
        return resp;
    }
    
    /**
     * Tính điểm trung bình cuối cùng theo công thức: (GVHD x1 + GVPB x2 + HĐ x1) / 4
     * Trong đó HĐ chỉ được tính khi có đủ 3 thành viên hội đồng chấm điểm
     */
    public FinalScoreResponse calculateFinalScore(Integer topicId) {
        log.info("Calculating final score for topic: {}", topicId);
        
        // Lấy tất cả đánh giá của topic
        List<ProjectEvaluation> evaluations = evaluationRepository.findByTopicId(topicId);
        
        if (evaluations.isEmpty()) {
            log.warn("No evaluations found for topic: {}", topicId);
            return null;
        }
        
        // Tính điểm trung bình cho từng loại đánh giá
        Double supervisorScore = getAverageScoreByType(evaluations, ProjectEvaluation.EvaluationType.SUPERVISOR);
        Double reviewerScore = getAverageScoreByType(evaluations, ProjectEvaluation.EvaluationType.REVIEWER);
        Double committeeScore = getCommitteeScoreWithTop3Members(evaluations);
        
        // Tính điểm cuối cùng: (GVHD x1 + GVPB x2 + HĐ x1) / 4
        Double finalScore = null;
        String status = "INCOMPLETE";
        
        if (supervisorScore != null && reviewerScore != null && committeeScore != null) {
            finalScore = (supervisorScore * 1 + reviewerScore * 2 + committeeScore * 1) / 4.0;
            status = "COMPLETED";
        } else if (supervisorScore != null || reviewerScore != null || committeeScore != null) {
            status = "INCOMPLETE";
        } else {
            status = "PENDING";
        }
        
        // Tạo response
        FinalScoreResponse response = new FinalScoreResponse();
        response.setTopicId(topicId);
        response.setSupervisorScore(supervisorScore != null ? supervisorScore.floatValue() : null);
        response.setReviewerScore(reviewerScore != null ? reviewerScore.floatValue() : null);
        response.setCommitteeScore(committeeScore != null ? committeeScore.floatValue() : null);
        response.setFinalScore(finalScore != null ? finalScore.floatValue() : null);
        response.setStatus(status);
        response.setEvaluations(evaluations.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList()));
        
        log.info("Final score calculated for topic {}: {}", topicId, finalScore);
        
        return response;
    }
    
    /**
     * Tính điểm trung bình theo loại đánh giá
     */
    private Double getAverageScoreByType(List<ProjectEvaluation> evaluations, ProjectEvaluation.EvaluationType type) {
        List<ProjectEvaluation> typeEvaluations = evaluations.stream()
                .filter(e -> e.getEvaluationType() == type && e.getTotalScore() != null)
                .collect(Collectors.toList());
        
        if (typeEvaluations.isEmpty()) {
            return null;
        }
        
        double sum = typeEvaluations.stream()
                .mapToDouble(ProjectEvaluation::getTotalScore)
                .sum();
        
        return sum / typeEvaluations.size();
    }
    
    /**
     * Tính điểm hội đồng: chỉ tính khi có đủ 3 thành viên hội đồng chấm điểm
     * Điểm hội đồng = (điểm thành viên 1 + điểm thành viên 2 + điểm thành viên 3) ÷ 3
     */
    private Double getCommitteeScoreWithTop3Members(List<ProjectEvaluation> evaluations) {
        List<ProjectEvaluation> committeeEvaluations = evaluations.stream()
                .filter(e -> e.getEvaluationType() == ProjectEvaluation.EvaluationType.COMMITTEE && e.getTotalScore() != null)
                .collect(Collectors.toList());
        
        // Chỉ tính điểm hội đồng khi có đủ 3 thành viên chấm điểm
        if (committeeEvaluations.size() < 3) {
            log.info("Committee score not calculated: need 3 members, got {}", committeeEvaluations.size());
            return null;
        }
        
        // Tính trung bình điểm của 3 thành viên hội đồng
        double averageScore = committeeEvaluations.stream()
                .mapToDouble(ProjectEvaluation::getTotalScore)
                .average()
                .orElse(0.0);
        
        log.info("Committee score calculated: {} members, average: {}", committeeEvaluations.size(), averageScore);
        return averageScore;
    }
    
    /**
     * Convert entity to response
     */
    private EvaluationResponse convertToResponse(ProjectEvaluation evaluation) {
        EvaluationResponse response = new EvaluationResponse();
        response.setEvaluationId(evaluation.getEvaluationId());
        response.setTopicId(evaluation.getTopicId());
        response.setStudentId(evaluation.getStudentId());
        response.setEvaluatorId(evaluation.getEvaluatorId());
        response.setEvaluationType(evaluation.getEvaluationType());
        response.setContentScore(evaluation.getContentScore());
        response.setPresentationScore(evaluation.getPresentationScore());
        response.setTechnicalScore(evaluation.getTechnicalScore());
        response.setInnovationScore(evaluation.getInnovationScore());
        response.setDefenseScore(evaluation.getDefenseScore());
        response.setTotalScore(evaluation.getTotalScore());
        response.setComments(evaluation.getComments());
        response.setEvaluatedAt(evaluation.getEvaluatedAt());
        response.setEvaluationStatus(evaluation.getEvaluationStatus());
        
        // Lấy thông tin đề tài từ thesis-service
        try {
            var topicInfo = thesisServiceClient.getTopicById(evaluation.getTopicId());
            if (topicInfo != null && topicInfo.get("title") != null) {
                response.setTopicTitle(topicInfo.get("title").toString());
            }
        } catch (Exception e) {
            log.warn("Could not fetch topic info for topicId {}: {}", evaluation.getTopicId(), e.getMessage());
        }
        
        // Lấy thông tin sinh viên từ StudentDefense nếu có
        try {
            var studentDefense = studentDefenseRepository.findByTopicId(evaluation.getTopicId());
            if (studentDefense.isPresent()) {
                var sd = studentDefense.get();
                response.setStudentName(sd.getStudentName());
                if (response.getTopicTitle() == null) {
                    response.setTopicTitle(sd.getTopicTitle());
                }
                // Set defense date and time
                if (sd.getDefenseSession() != null) {
                    response.setDefenseDate(sd.getDefenseSession().getDefenseDate());
                    response.setDefenseTime(sd.getDefenseSession().getStartTime());
                }
            }
        } catch (Exception e) {
            log.warn("Could not fetch student defense info for topicId {}: {}", evaluation.getTopicId(), e.getMessage());
        }
        
        return response;
    }
    
    /**
     * Lấy thông tin chi tiết đề tài cho giảng viên chấm điểm
     */
    public Map<String, Object> getTopicDetailsForGrading(Integer topicId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Lấy thông tin đề tài từ thesis-service
            var topicInfo = thesisServiceClient.getTopicById(topicId);
            if (topicInfo != null) {
                result.put("topic", topicInfo);
            }
            
            // Lấy thông tin sinh viên từ StudentDefense
            var studentDefense = studentDefenseRepository.findByTopicId(topicId);
            if (studentDefense.isPresent()) {
                var sd = studentDefense.get();
                Map<String, Object> studentInfo = new HashMap<>();
                studentInfo.put("studentId", sd.getStudentId());
                studentInfo.put("studentName", sd.getStudentName());
                studentInfo.put("studentMajor", sd.getStudentMajor());
                studentInfo.put("supervisorId", sd.getSupervisorId());
                studentInfo.put("defenseOrder", sd.getDefenseOrder());
                studentInfo.put("defenseTime", sd.getDefenseTime());
                studentInfo.put("durationMinutes", sd.getDurationMinutes());
                studentInfo.put("status", sd.getStatus());
                studentInfo.put("score", sd.getScore());
                studentInfo.put("comments", sd.getComments());
                
                // Thông tin buổi bảo vệ
                if (sd.getDefenseSession() != null) {
                    var session = sd.getDefenseSession();
                    Map<String, Object> sessionInfo = new HashMap<>();
                    sessionInfo.put("sessionId", session.getSessionId());
                    sessionInfo.put("sessionName", session.getSessionName());
                    sessionInfo.put("defenseDate", session.getDefenseDate());
                    sessionInfo.put("startTime", session.getStartTime());
                    sessionInfo.put("endTime", session.getEndTime());
                    sessionInfo.put("location", session.getLocation());
                    sessionInfo.put("status", session.getStatus());
                    studentInfo.put("defenseSession", sessionInfo);
                }
                
                result.put("student", studentInfo);
            }
            
            // Lấy các đánh giá hiện có
            var evaluations = evaluationRepository.findByTopicId(topicId);
            result.put("evaluations", evaluations.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList()));
            
            // Lấy điểm cuối cùng nếu có
            var finalScore = calculateFinalScore(topicId);
            if (finalScore != null) {
                result.put("finalScore", finalScore);
            }
            
        } catch (Exception e) {
            log.error("Error getting topic details for topicId {}: {}", topicId, e.getMessage(), e);
            throw new RuntimeException("Could not fetch topic details", e);
        }
        
        return result;
    }

    /**
     * Validate scores based on role-specific criteria
     */
    private void validateScoresByRole(EvaluationRequest request) {
        var type = request.getEvaluationType();
        
        if (type == ProjectEvaluation.EvaluationType.COMMITTEE) {
            // Hội đồng: 6 tiêu chí
            validateScoreRange(request.getPresentationClarityScore(), 0.0f, 0.5f, "Trình bày nội dung");
            validateScoreRange(request.getReviewerQaScore(), 0.0f, 1.5f, "Trả lời câu hỏi GVPB");
            validateScoreRange(request.getCommitteeQaScore(), 0.0f, 1.5f, "Trả lời câu hỏi hội đồng");
            validateScoreRange(request.getAttitudeScore(), 0.0f, 1.0f, "Tinh thần, thái độ");
            validateScoreRange(request.getContentImplementationScore(), 0.0f, 4.5f, "Thực hiện nội dung đề tài");
            validateScoreRange(request.getRelatedIssuesScore(), 0.0f, 1.0f, "Mối liên hệ vấn đề liên quan");
        } else if (type == ProjectEvaluation.EvaluationType.REVIEWER) {
            // Giảng viên phản biện: 5 tiêu chí
            validateScoreRange(request.getFormatScore(), 0.0f, 1.5f, "Hình thức trình bày");
            validateScoreRange(request.getContentQualityScore(), 0.0f, 4.0f, "Thực hiện nội dung đề tài");
            validateScoreRange(request.getRelatedIssuesReviewerScore(), 0.0f, 2.0f, "Mối liên hệ vấn đề liên quan");
            validateScoreRange(request.getPracticalApplicationScore(), 0.0f, 2.0f, "Tính ứng dụng thực tiễn");
            validateScoreRange(request.getBonusScore(), 0.0f, 0.5f, "Điểm thưởng");
        } else if (type == ProjectEvaluation.EvaluationType.SUPERVISOR) {
            // Giảng viên hướng dẫn: 6 tiêu chí
            validateScoreRange(request.getStudentAttitudeScore(), 0.0f, 1.0f, "Ý thức, thái độ sinh viên");
            validateScoreRange(request.getProblemSolvingScore(), 0.0f, 1.0f, "Khả năng xử lý vấn đề");
            validateScoreRange(request.getFormatSupervisorScore(), 0.0f, 1.5f, "Hình thức trình bày");
            validateScoreRange(request.getContentImplementationSupervisorScore(), 0.0f, 4.5f, "Thực hiện nội dung đề tài");
            validateScoreRange(request.getRelatedIssuesSupervisorScore(), 0.0f, 1.0f, "Mối liên hệ vấn đề liên quan");
            validateScoreRange(request.getPracticalApplicationSupervisorScore(), 0.0f, 1.0f, "Tính ứng dụng thực tiễn");
        }
    }

    private void validateScoreRange(Float score, float min, float max, String fieldName) {
        if (score != null && (score < min || score > max)) {
            throw new IllegalArgumentException(String.format("%s phải trong khoảng %.1f - %.1f điểm", fieldName, min, max));
        }
    }

    /**
     * Map scores from request to entity based on role
     */
    private void mapScoresByRole(EvaluationRequest request, ProjectEvaluation evaluation) {
        var type = request.getEvaluationType();
        
        // Clear old scores first
        clearAllScores(evaluation);
        
        if (type == ProjectEvaluation.EvaluationType.COMMITTEE) {
            // Hội đồng: 6 tiêu chí
            evaluation.setPresentationClarityScore(request.getPresentationClarityScore());
            evaluation.setReviewerQaScore(request.getReviewerQaScore());
            evaluation.setCommitteeQaScore(request.getCommitteeQaScore());
            evaluation.setAttitudeScore(request.getAttitudeScore());
            evaluation.setContentImplementationScore(request.getContentImplementationScore());
            evaluation.setRelatedIssuesScore(request.getRelatedIssuesScore());
        } else if (type == ProjectEvaluation.EvaluationType.REVIEWER) {
            // Giảng viên phản biện: 5 tiêu chí
            evaluation.setFormatScore(request.getFormatScore());
            evaluation.setContentQualityScore(request.getContentQualityScore());
            evaluation.setRelatedIssuesReviewerScore(request.getRelatedIssuesReviewerScore());
            evaluation.setPracticalApplicationScore(request.getPracticalApplicationScore());
            evaluation.setBonusScore(request.getBonusScore());
        } else if (type == ProjectEvaluation.EvaluationType.SUPERVISOR) {
            // Giảng viên hướng dẫn: 6 tiêu chí
            evaluation.setStudentAttitudeScore(request.getStudentAttitudeScore());
            evaluation.setProblemSolvingScore(request.getProblemSolvingScore());
            evaluation.setFormatSupervisorScore(request.getFormatSupervisorScore());
            evaluation.setContentImplementationSupervisorScore(request.getContentImplementationSupervisorScore());
            evaluation.setRelatedIssuesSupervisorScore(request.getRelatedIssuesSupervisorScore());
            evaluation.setPracticalApplicationSupervisorScore(request.getPracticalApplicationSupervisorScore());
        }
    }

    private void clearAllScores(ProjectEvaluation evaluation) {
        // Clear committee scores
        evaluation.setPresentationClarityScore(null);
        evaluation.setReviewerQaScore(null);
        evaluation.setCommitteeQaScore(null);
        evaluation.setAttitudeScore(null);
        evaluation.setContentImplementationScore(null);
        evaluation.setRelatedIssuesScore(null);
        
        // Clear reviewer scores
        evaluation.setFormatScore(null);
        evaluation.setContentQualityScore(null);
        evaluation.setRelatedIssuesReviewerScore(null);
        evaluation.setPracticalApplicationScore(null);
        evaluation.setBonusScore(null);
        
        // Clear supervisor scores
        evaluation.setStudentAttitudeScore(null);
        evaluation.setProblemSolvingScore(null);
        evaluation.setFormatSupervisorScore(null);
        evaluation.setContentImplementationSupervisorScore(null);
        evaluation.setRelatedIssuesSupervisorScore(null);
        evaluation.setPracticalApplicationSupervisorScore(null);
    }
}
