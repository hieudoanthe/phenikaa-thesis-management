package com.phenikaa.evalservice.service;

import com.phenikaa.evalservice.client.ProfileServiceClient;
import com.phenikaa.evalservice.client.ThesisServiceClient;
import com.phenikaa.evalservice.dto.*;
import com.phenikaa.evalservice.dto.request.ConfirmAutoAssignRequest;
import com.phenikaa.evalservice.entity.DefenseSession;
import com.phenikaa.evalservice.repository.DefenseSessionRepository;
import com.phenikaa.evalservice.repository.DefenseCommitteeRepository;
import com.phenikaa.evalservice.service.interfaces.AiAssignService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AutoAssignService {

    private final ThesisServiceClient thesisServiceClient;
    private final ProfileServiceClient profileServiceClient;
    private final DefenseSessionRepository defenseSessionRepository;
    private final DefenseCommitteeRepository defenseCommitteeRepository;
    private final AiAssignService aiAssignService;
    private final StudentAssignmentService studentAssignmentService;
    private final DefenseSessionService defenseSessionService;

    public AutoAssignPreviewResponse preview(AutoAssignPreviewRequest req) {
        try {
            if (req == null || req.getPeriodId() == null) {
                return AutoAssignPreviewResponse.builder()
                        .success(false)
                        .message("periodId is required")
                        .build();
            }

            // 1) Lấy sinh viên theo đợt (phân trang - lấy nhiều nhất 1000 dòng cho preview)
            Map<String, Object> page = thesisServiceClient.getAllStudentsByPeriod(String.valueOf(req.getPeriodId()), 0, 1000);
            Object content = page != null ? page.get("content") : null;
            List<Map<String, Object>> students = (content instanceof List)
                    ? ((List<Map<String, Object>>) content).stream()
                    .filter(Objects::nonNull)
                    .filter(s -> "APPROVED".equalsIgnoreCase(String.valueOf(s.get("suggestionStatus"))))
                    .collect(Collectors.toList())
                    : Collections.emptyList();

            // 2) Lấy danh sách giảng viên (từ profile-service per lecturerId khi cần) – ở đây lazy khi match

            // 3) Chuẩn hóa & từ khóa trọng số
            // Helper normalize tiếng Việt
            final Map<String, Map<String,Integer>> WEIGHTED = buildWeightedKeywords();

            // Lấy tất cả buổi hiện có (loại bỏ buổi không hợp lệ nếu cần)
            List<DefenseSession> existing = defenseSessionRepository.findAll();
            List<SessionPreviewDto> sessions = new ArrayList<>();
            Map<Integer, Integer> currentCounts = new HashMap<>();
            // sessionId -> reviewerIds
            Map<Integer, List<Integer>> sessionReviewers = new HashMap<>();
            // reviewerId -> specialization
            Map<Integer, String> reviewerSpecialization = new HashMap<>();

            for (DefenseSession ds : existing) {
                if (ds == null) continue;
                Integer max = ds.getMaxStudents() == null ? 5 : ds.getMaxStudents();
                if (max != null && max <= 0) continue;
                sessions.add(SessionPreviewDto.builder()
                        .sessionId(String.valueOf(ds.getSessionId()))
                        .sessionName(ds.getSessionName())
                        .location(ds.getLocation())
                        .defenseDate(ds.getDefenseDate().atStartOfDay())
                        .startTime(ds.getStartTime())
                        .maxStudents(max)
                        .virtualSession(false)
                        .students(new ArrayList<>())
                        .build());
                currentCounts.put(ds.getSessionId(), 0);

                // Load reviewers for this session
                var committees = defenseCommitteeRepository.findByDefenseSession_SessionId(ds.getSessionId());
                List<Integer> reviewers = new ArrayList<>();
                if (committees != null) {
                    for (var c : committees) {
                        if (c != null && c.getRole() == com.phenikaa.evalservice.entity.DefenseCommittee.CommitteeRole.REVIEWER && c.getLecturerId() != null) {
                            reviewers.add(c.getLecturerId());
                            // cache specialization
                            if (!reviewerSpecialization.containsKey(c.getLecturerId())) {
                                try {
                                    var profile = profileServiceClient.getTeacherProfile(c.getLecturerId());
                                    String spec = stringOr(profile != null ? profile.get("specialization") : null, "");
                                    reviewerSpecialization.put(c.getLecturerId(), spec);
                                } catch (Exception ignored) {}
                            }
                        }
                    }
                }
                sessionReviewers.put(ds.getSessionId(), reviewers);
            }

            List<StudentPreviewDto> unassigned = new ArrayList<>();

            for (Map<String,Object> st : students) {
                Integer studentId = toInt(st.get("studentId"));
                String studentName = stringOr(st.get("fullName"), "Sinh viên " + studentId);
                String topicTitle = stringOr(st.get("topicTitle"), "");

                // Detect major by weighted keywords
                String major = detectMajor(topicTitle, WEIGHTED);

                // Reviewer sẽ lấy từ buổi có sẵn, không còn đoán theo supervisor
                Integer reviewerId = null;
                String reviewerName = null;

                // Hai pha: 1) chỉ các buổi match tuyệt đối; 2) buổi liên quan; 3) bất kỳ buổi còn chỗ
                List<SessionPreviewDto> available = sessions.stream()
                        .filter(s -> (s.getStudents() != null ? s.getStudents().size() : 0) < (s.getMaxStudents() != null ? s.getMaxStudents() : 5))
                        .collect(Collectors.toList());

                java.util.function.ToIntFunction<SessionPreviewDto> sizeFn = s -> s.getStudents() != null ? s.getStudents().size() : 0;

                // Pha 1: exact match
                List<SessionPreviewDto> exact = available.stream()
                        .filter(s -> hasExactMatchReviewer(sessionReviewers, reviewerSpecialization, toIntSafe(s.getSessionId()), major))
                        .sorted(Comparator.comparingInt(sizeFn).reversed())
                        .collect(Collectors.toList());
                SessionPreviewDto ses = null;
                if (!exact.isEmpty()) {
                    ses = exact.get(0);
                } else {
                    // Pha 2: related match (AI <-> Data)
                    List<SessionPreviewDto> related = available.stream()
                            .filter(s -> hasRelatedMatchReviewer(sessionReviewers, reviewerSpecialization, toIntSafe(s.getSessionId()), major))
                            .sorted(Comparator.comparingInt(sizeFn).reversed())
                            .collect(Collectors.toList());
                    if (!related.isEmpty()) {
                        ses = related.get(0);
                    } else {
                        // Pha 3: bất kỳ buổi còn chỗ
                        ses = available.stream()
                                .sorted(Comparator.comparingInt(sizeFn).reversed())
                                .findFirst()
                                .orElse(null);
                    }
                }
                // Lấy reviewer name/id hiển thị theo buổi đã chọn
                if (ses != null) {
                    Integer sid = toIntSafe(ses.getSessionId());
                    List<Integer> revs = sessionReviewers.getOrDefault(sid, Collections.emptyList());
                    if (!revs.isEmpty()) {
                        reviewerId = revs.get(0);
                        try {
                            var prof = profileServiceClient.getTeacherProfile(reviewerId);
                            reviewerName = prof != null ? stringOr(prof.get("fullName"), "Giảng viên " + reviewerId) : ("Giảng viên " + reviewerId);
                        } catch (Exception e) {
                            reviewerName = "Giảng viên " + reviewerId;
                        }
                    }
                }
                if (ses == null) {
                    unassigned.add(StudentPreviewDto.builder()
                            .studentId(studentId)
                            .studentName(studentName)
                            .topicTitle(topicTitle)
                            .reviewerId(reviewerId)
                            .reviewerName(reviewerName)
                            .build());
                    continue;
                }
                if (ses.getStudents() == null) ses.setStudents(new ArrayList<>());
                ses.getStudents().add(StudentPreviewDto.builder()
                        .studentId(studentId)
                        .studentName(studentName)
                        .topicTitle(topicTitle)
                        .reviewerId(reviewerId)
                        .reviewerName(reviewerName)
                        .build());
            }

            if (!unassigned.isEmpty()) {
                sessions.add(SessionPreviewDto.builder()
                        .sessionId("unassigned")
                        .sessionName("Sinh viên chưa được sắp xếp vào buổi nào")
                        .location("-")
                        .defenseDate(LocalDateTime.now())
                        .maxStudents(0)
                        .virtualSession(true)
                        .students(unassigned)
                        .build());
            }

            return AutoAssignPreviewResponse.builder()
                    .success(true)
                    .sessions(sessions)
                    .message("OK")
                    .build();
        } catch (Exception e) {
            log.error("preview auto-assign error: {}", e.getMessage(), e);
            return AutoAssignPreviewResponse.builder()
                    .success(false)
                    .message("Error: " + e.getMessage())
                    .build();
        }
    }

    public ConfirmAutoAssignResponse confirm(ConfirmAutoAssignRequest req) {
        int totalAssigned = 0;
        int createdSessions = 0;
        try {
            if (req == null || req.getAssignments() == null || req.getAssignments().isEmpty()) {
                return ConfirmAutoAssignResponse.builder()
                        .success(false)
                        .totalAssigned(0)
                        .createdSessions(0)
                        .message("No assignments provided")
                        .build();
            }

            Integer scheduleId = req.getScheduleId();
            for (ConfirmAutoAssignRequest.SessionAssignmentDto s : req.getAssignments()) {
                Integer sessionId = null;
                boolean needCreate = (s.getSessionId() == null)
                        || s.getSessionId().startsWith("preview-")
                        || s.getSessionId().isBlank();
                DefenseSession session;
                if (needCreate) {
                    // create new session with minimal info
                    DefenseSessionDto dto = DefenseSessionDto.builder()
                            .sessionName(s.getSessionName())
                            .scheduleId(scheduleId)
                            .defenseDate(s.getDefenseDate() != null ? s.getDefenseDate().toLocalDate() : null)
                            .startTime(s.getDefenseDate())
                            .endTime(s.getDefenseDate() != null ? s.getDefenseDate().plusHours(1) : null)
                            .location(s.getLocation())
                            .maxStudents(s.getStudents() != null ? Math.max(5, s.getStudents().size()) : 5)
                            .notes("Created by AI confirm")
                            .build();
                    DefenseSessionDto created = defenseSessionService.createSession(dto);
                    sessionId = created.getSessionId();
                    createdSessions++;
                } else {
                    try {
                        sessionId = Integer.valueOf(s.getSessionId());
                    } catch (Exception ignored) {}
                }
                if (sessionId == null) {
                    continue;
                }
                // assign students
                if (s.getStudents() != null) {
                    for (ConfirmAutoAssignRequest.StudentAssignDto st : s.getStudents()) {
                        if (st == null || st.getStudentId() == null) continue;
                        // enrich topic title if missing is optional; assign with minimum fields
                        boolean ok = assignStudentToSessionInternal(sessionId, st);
                        if (ok) totalAssigned++;
                    }
                }
            }
            return ConfirmAutoAssignResponse.builder()
                    .success(true)
                    .totalAssigned(totalAssigned)
                    .createdSessions(createdSessions)
                    .message("Confirmed")
                    .build();
        } catch (Exception e) {
            return ConfirmAutoAssignResponse.builder()
                    .success(false)
                    .totalAssigned(totalAssigned)
                    .createdSessions(createdSessions)
                    .message("Error: " + e.getMessage())
                    .build();
        }
    }

    private boolean assignStudentToSessionInternal(Integer sessionId, ConfirmAutoAssignRequest.StudentAssignDto st) {
        try {
            Integer topicId = st.getTopicId();
            String studentName = st.getStudentName();
            String major = st.getSpecialization();
            String topicTitle = st.getTopicTitle();
            
            // Bắt buộc phải có topicId, nếu không có thì skip
            if (topicId == null) {
                log.warn("Sinh viên {} không có topicId, bỏ qua gán", st.getStudentId());
                return false;
            }
            
            // Lấy thông tin từ profile-service
            try {
                var profile = profileServiceClient.getStudentProfile(st.getStudentId());
                if (profile != null) {
                    if (studentName == null) studentName = stringOr(profile.get("fullName"), "Sinh viên " + st.getStudentId());
                    if (major == null) major = stringOr(profile.get("major"), "Công nghệ thông tin");
                }
            } catch (Exception e) {
                log.warn("Không thể lấy profile sinh viên {}: {}", st.getStudentId(), e.getMessage());
            }
            
            
            // Đảm bảo có giá trị mặc định
            if (studentName == null) studentName = "Sinh viên " + st.getStudentId();
            if (major == null) major = "Công nghệ thông tin";
            
            if (topicTitle == null || topicTitle.isBlank()) {
                try {
                    var topic = thesisServiceClient.getTopicById(topicId);
                    if (topic != null) {
                        String t1 = stringOr(topic.get("topicTitle"), null);
                        String t2 = stringOr(topic.get("title"), null);
                        topicTitle = (t1 != null && !t1.isBlank()) ? t1 : (t2 != null ? t2 : "N/A");
                    }
                } catch (Exception ignored) {}
            }
            
            if (topicTitle == null) topicTitle = "N/A";
            
            return studentAssignmentService.assignStudentToSession(
                    sessionId,
                    st.getStudentId(),
                    topicId,
                    null, // Bỏ supervisorId
                    studentName,
                    major,
                    topicTitle
            );
        } catch (Exception e) {
            log.error("Lỗi khi gán sinh viên {} vào buổi {}: {}", st.getStudentId(), sessionId, e.getMessage());
            return false;
        }
    }

    public AutoAssignPreviewResponse previewWithGemini(AutoAssignPreviewRequest req) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("periodId", req != null ? req.getPeriodId() : null);

        // Collect students for the given period (limit reasonable page size)
        List<Map<String, Object>> studentInputs = new ArrayList<>();
        try {
            Map<String, Object> page = thesisServiceClient.getAllStudentsByPeriod(String.valueOf(req.getPeriodId()), 0, 1000);
            Object content = page != null ? page.get("content") : null;
            List<Map<String, Object>> students = (content instanceof List)
                    ? ((List<Map<String, Object>>) content).stream()
                    .filter(Objects::nonNull)
                    .filter(s -> "APPROVED".equalsIgnoreCase(String.valueOf(s.get("suggestionStatus"))))
                    .collect(Collectors.toList())
                    : Collections.emptyList();
            for (Map<String, Object> s : students) {
                Map<String, Object> si = new HashMap<>();
                Integer stId = toInt(s.get("studentId"));
                si.put("studentId", stId);
                si.put("fullName", s.get("fullName"));

                // enrich topic title if missing
                String topicTitle = stringOr(s.get("topicTitle"), "");
                Integer topicId = toInt(s.get("topicId"));
                if ((topicTitle == null || topicTitle.isBlank()) && topicId != null) {
                    try {
                        var topic = thesisServiceClient.getTopicById(topicId);
                        if (topic != null) {
                            String t1 = stringOr(topic.get("topicTitle"), null);
                            String t2 = stringOr(topic.get("title"), null);
                            topicTitle = (t1 != null && !t1.isBlank()) ? t1 : (t2 != null ? t2 : "");
                        }
                    } catch (Exception ignored) {}
                }
                if (topicTitle == null) topicTitle = "";
                si.put("topicTitle", topicTitle);
                studentInputs.add(si);
            }
        } catch (Exception ignored) {}
        payload.put("students", studentInputs);

        // Collect existing sessions and reviewer context
        List<Map<String, Object>> sessionInputs = new ArrayList<>();
        Map<Integer, List<Integer>> sessionReviewers = new HashMap<>();
        Map<Integer, String> reviewerSpecialization = new HashMap<>();
        try {
            List<DefenseSession> existing = defenseSessionRepository.findAll();
            for (DefenseSession ds : existing) {
                if (ds == null) continue;
                Map<String, Object> si = new HashMap<>();
                si.put("sessionId", String.valueOf(ds.getSessionId()));
                si.put("sessionName", ds.getSessionName());
                si.put("location", ds.getLocation());
                si.put("defenseDate", ds.getDefenseDate() != null ? ds.getDefenseDate().atStartOfDay() : null);
                si.put("maxStudents", ds.getMaxStudents() == null ? 5 : ds.getMaxStudents());
                sessionInputs.add(si);

                // reviewers
                var committees = defenseCommitteeRepository.findByDefenseSession_SessionId(ds.getSessionId());
                List<Integer> reviewers = new ArrayList<>();
                if (committees != null) {
                    for (var c : committees) {
                        if (c != null && c.getRole() == com.phenikaa.evalservice.entity.DefenseCommittee.CommitteeRole.REVIEWER && c.getLecturerId() != null) {
                            reviewers.add(c.getLecturerId());
                            if (!reviewerSpecialization.containsKey(c.getLecturerId())) {
                                try {
                                    var profile = profileServiceClient.getTeacherProfile(c.getLecturerId());
                                    String spec = stringOr(profile != null ? profile.get("specialization") : null, "");
                                    reviewerSpecialization.put(c.getLecturerId(), spec);
                                } catch (Exception ignored) {}
                            }
                        }
                    }
                }
                sessionReviewers.put(ds.getSessionId(), reviewers);
            }
        } catch (Exception ignored) {}
        payload.put("sessions", sessionInputs);
        payload.put("sessionReviewers", sessionReviewers);
        payload.put("reviewerSpecialization", reviewerSpecialization);

        // Directly use internal AI service instead of external ThesisAiClient
        AutoAssignPreviewResponse aiResp = aiAssignService.generatePreview(payload);

        // Enrich reviewer info when missing using known sessionReviewers/profile service
        try {
            if (aiResp != null && aiResp.getSessions() != null) {
                for (SessionPreviewDto ses : aiResp.getSessions()) {
                    Integer sid = null;
                    try { sid = ses.getSessionId() != null ? Integer.valueOf(ses.getSessionId()) : null; } catch (Exception ignored) {}
                    List<Integer> revs = sid != null ? sessionReviewers.getOrDefault(sid, java.util.Collections.emptyList()) : java.util.Collections.emptyList();
                    // Enrich startTime from DB for existing sessions
                    if (sid != null) {
                        try {
                            Optional<DefenseSession> sOpt = defenseSessionRepository.findById(sid);
                            if (sOpt.isPresent() && sOpt.get().getStartTime() != null) {
                                ses.setStartTime(sOpt.get().getStartTime());
                            }
                            if (sOpt.isPresent() && sOpt.get().getDefenseDate() != null && (ses.getDefenseDate() == null)) {
                                ses.setDefenseDate(sOpt.get().getDefenseDate().atStartOfDay());
                            }
                            if (sOpt.isPresent() && sOpt.get().getLocation() != null && (ses.getLocation() == null || ses.getLocation().isBlank())) {
                                ses.setLocation(sOpt.get().getLocation());
                            }
                        } catch (Exception ignored) {}
                    }
                    if (ses.getStudents() == null) continue;
                    for (StudentPreviewDto st : ses.getStudents()) {
                        if ((st.getReviewerId() == null || st.getReviewerId() <= 0) && !revs.isEmpty()) {
                            st.setReviewerId(revs.get(0));
                        }
                        if ((st.getReviewerName() == null || st.getReviewerName().isBlank()) && st.getReviewerId() != null) {
                            try {
                                var prof = profileServiceClient.getTeacherProfile(st.getReviewerId());
                                String rname = stringOr(prof != null ? prof.get("fullName") : null, null);
                                if (rname != null && !rname.isBlank()) st.setReviewerName(rname);
                                String rspec = stringOr(prof != null ? prof.get("specialization") : null, null);
                                if (rspec != null && !rspec.isBlank()) st.setReviewerSpecialization(rspec);
                            } catch (Exception ignored) {}
                        }
                    }
                }
            }
        } catch (Exception ignored) {}

        return aiResp;
    }

    // ============== helpers ==================
    private static String stringOr(Object v, String dft) {
        return v == null ? dft : String.valueOf(v);
    }
    private static Integer toInt(Object v) {
        try { return v == null ? null : Integer.valueOf(String.valueOf(v)); } catch (Exception e) { return null; }
    }
    private static Integer toIntSafe(Object v) {
        try { return Integer.valueOf(String.valueOf(v)); } catch (Exception e) { return null; }
    }
    private static LocalDateTime parseDateTimeOrNow(Object v) {
        try {
            if (v == null) return LocalDateTime.now();
            if (v instanceof LocalDateTime ldt) return ldt;
            return LocalDateTime.parse(String.valueOf(v));
        } catch (Exception e) {
            return LocalDateTime.now();
        }
    }
    private static String normalizeMajor(String text) {
        String n = normalizeVi(text);
        // Map rõ ràng theo chuyên ngành thực tế của giảng viên
        if (n.contains("cong nghe phan mem") || n.contains("ky thuat phan mem")) return "phần mềm ứng dụng"; // Software Engineering
        if (n.contains("khoa hoc may tinh")) return "trí tuệ nhân tạo"; // Computer Science → bias to AI/ML
        if (n.contains("mang may tinh") || n.contains("truyen thong du lieu")) return "mạng máy tính";
        if (n.contains("he thong thong tin quan ly") || n.contains("he thong thong tin")) return "hệ thống thông tin";
        if (n.contains("tri tue") || n.contains("ai") || n.contains("ml") || n.contains("deep")) return "trí tuệ nhân tạo";
        if (n.contains("khoa hoc du lieu") || n.contains("data") || n.contains("big data") || n.contains("etl")) return "khoa học dữ liệu";
        if (n.contains("iot") || n.contains("nhung") || n.contains("sensor") || n.contains("arduino") || n.contains("esp32")) return "iot";
        if (n.contains("bao mat") || n.contains("security") || n.contains("an toan")) return "an toàn thông tin";
        if (n.contains("cloud") || n.contains("aws") || n.contains("azure") || n.contains("gcp") || n.contains("kubernetes") || n.contains("docker")) return "cloud";
        return "phần mềm ứng dụng";
    }
    private static boolean hasExactMatchReviewer(Map<Integer, List<Integer>> sessionReviewers,
                                                Map<Integer, String> reviewerSpec,
                                                Integer sessionId,
                                                String studentMajor) {
        if (sessionId == null) return false;
        List<Integer> revs = sessionReviewers.get(sessionId);
        if (revs == null || revs.isEmpty()) return false;
        for (Integer r : revs) {
            String spec = reviewerSpec.get(r);
            if (spec != null && spec.equals(studentMajor)) return true;
        }
        return false;
    }

    private static boolean hasRelatedMatchReviewer(Map<Integer, List<Integer>> sessionReviewers,
                                                   Map<Integer, String> reviewerSpec,
                                                   Integer sessionId,
                                                   String studentMajor) {
        if (sessionId == null) return false;
        List<Integer> revs = sessionReviewers.get(sessionId);
        if (revs == null || revs.isEmpty()) return false;
        for (Integer r : revs) {
            String spec = reviewerSpec.get(r);
            if (spec != null && areRelatedMajors(spec, studentMajor)) return true;
        }
        return false;
    }

    private static boolean areRelatedMajors(String a, String b) {
        // AI <-> Data Science coi là gần
        return ("trí tuệ nhân tạo".equals(a) && "khoa học dữ liệu".equals(b))
                || ("khoa học dữ liệu".equals(a) && "trí tuệ nhân tạo".equals(b));
    }
    private static String normalizeVi(String s) {
        if (s == null) return "";
        String n = Normalizer.normalize(s, Normalizer.Form.NFD)
                .replaceAll("[\\u0300-\\u036f]","")
                .toLowerCase()
                .replaceAll("[^a-z0-9\\s]"," ")
                .replaceAll("\\s+"," ")
                .trim();
        return n;
    }
    private static String detectMajor(String title, Map<String, Map<String,Integer>> weighted) {
        String t = normalizeVi(title);
        String best = "phần mềm ứng dụng";
        int bestScore = 0;
        for (var e : weighted.entrySet()) {
            int s = 0;
            for (var kw : e.getValue().entrySet()) {
                if (t.contains(kw.getKey())) s += kw.getValue();
            }
            if (s > bestScore) { bestScore = s; best = e.getKey(); }
        }
        return best;
    }
    private static Map<String, Map<String,Integer>> buildWeightedKeywords() {
        Map<String, Map<String,Integer>> w = new LinkedHashMap<>();
        w.put("phần mềm ứng dụng", mapOf(new String[][]{
                {"web","2"},{"ung dung","3"},{"phan mem","3"},{"mobile","2"},
                {"spring","1"},{"react","1"},{"node","1"},{"flutter","1"},{"android","1"},{"ios","1"},{"devops","1"},
                {"quan ly","2"},{"he thong","1"},{"website","2"},{"ban hang","2"},{"thuong mai dien tu","3"},
                {"quan ly benh vien","3"},{"quan ly phong kham","3"},{"dat lich","2"},{"gia su","1"},{"hoc truc tuyen","2"},
                {"kanban","1"},{"quan ly du an","1"},{"quan ly kho","2"},{"chuoi cung ung","2"},{"dat ve","2"},
                {"ngan hang","1"},{"quan ly nha hang","2"}
        }));
        w.put("khoa học dữ liệu", mapOf(new String[][]{
                {"khoa hoc du lieu","3"},{"khai pha du lieu","2"},{"data","1"},{"etl","1"},{"pandas","1"},{"warehouse","1"},{"big data","2"}
        }));
        w.put("trí tuệ nhân tạo", mapOf(new String[][]{
                {"tri tue nhan tao","4"},{"hoc may","3"},{"hoc sau","3"},{"ai","3"},{"ml","3"},{"deep learning","4"},
                {"cnn","2"},{"rnn","2"},{"nlp","3"},{"yolo","2"},{"transformer","3"},{"bert","2"},{"gpt","2"},
                {"predict","2"},{"classification","2"},{"regression","2"},{"khuyen nghi","3"},{"goi y","2"},
                {"chatbot","2"},{"giong noi","3"},{"nhan dang","3"},{"cam xuc","2"},{"agent ai","3"},
                {"recommender","2"}
        }));
        w.put("mạng máy tính", mapOf(new String[][]{
                {"mang may tinh","3"},{"truyen thong du lieu","3"},{"tcp/ip","2"},{"routing","2"},{"switching","2"},{"lan","1"},{"wan","1"},
                {"trung tam du lieu","3"},{"giam sat ha tang","3"}
        }));
        w.put("iot", mapOf(new String[][]{
                {"iot","4"},{"cam bien","3"},{"sensor","3"},{"raspberry","2"},{"arduino","2"},{"esp32","2"},{"nhung","2"},{"edge","1"},
                {"rfid","3"},{"bien so","2"}
        }));
        w.put("an toàn thông tin", mapOf(new String[][]{
                {"an toan thong tin","4"},{"bao mat","3"},{"security","3"},{"ma hoa","2"},{"tan cong","2"},{"xss","2"},{"sql injection","2"},{"malware","2"}
        }));
        w.put("hệ thống thông tin", mapOf(new String[][]{
                {"he thong thong tin","3"},{"he thong thong tin quan ly","4"},{"quan ly","2"},{"erp","2"},{"crm","2"},{"business","1"},{"quan tri","1"}
        }));
        w.put("cloud", mapOf(new String[][]{
                {"dien toan dam may","3"},{"cloud","3"},{"aws","2"},{"gcp","2"},{"azure","2"},{"kubernetes","2"},{"docker","2"},{"terraform","1"},
                {"microservice","3"},{"blockchain","3"},{"web3","3"},{"airdrop","2"}
        }));
        return w;
    }
    private static Map<String,Integer> mapOf(String[][] kv) {
        Map<String,Integer> m = new LinkedHashMap<>();
        for (String[] e : kv) m.put(e[0], Integer.parseInt(e[1]));
        return m;
    }
}


