package com.phenikaa.evalservice.service.implement;

import com.phenikaa.evalservice.service.interfaces.AiAssignService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import com.phenikaa.evalservice.dto.AutoAssignPreviewResponse;
import com.phenikaa.evalservice.dto.SessionPreviewDto;
import com.phenikaa.evalservice.dto.StudentPreviewDto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AiAssignServiceImpl implements AiAssignService {
    @Value("${ai.gemini.api-key:}")
    private String geminiApiKey;
    @Value("${ai.gemini.model-name:gemini-1.5-pro}")
    private String geminiModelName;

    @Override
    public AutoAssignPreviewResponse generatePreview(Map<String, Object> payload) {
        try {
            // Try real Gemini via REST if configured
            String aiJson = callGeminiRest(payload);
            if (aiJson != null && !aiJson.isBlank()) {
                // Clean code fences if present
                String cleaned = aiJson.trim();
                if (cleaned.startsWith("```")) {
                    int first = cleaned.indexOf('\n');
                    int last = cleaned.lastIndexOf("```");
                    if (first >= 0 && last > first) cleaned = cleaned.substring(first + 1, last).trim();
                }
                ObjectMapper mapper = new ObjectMapper();
                Map<String, Object> resp = mapper.readValue(cleaned, new TypeReference<Map<String, Object>>(){});

                boolean success = resp != null && Boolean.TRUE.equals(resp.get("success"));
                String message = resp != null ? String.valueOf(resp.getOrDefault("message", "")) : "";
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> sesRaw = resp != null && resp.get("sessions") instanceof List ? (List<Map<String, Object>>) resp.get("sessions") : List.of();

                List<SessionPreviewDto> sessions = new ArrayList<>();
                for (Map<String, Object> s : sesRaw) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> stu = s.get("students") instanceof List ? (List<Map<String, Object>>) s.get("students") : List.of();
                    List<StudentPreviewDto> students = new ArrayList<>();
                    for (Map<String, Object> x : stu) {
                        students.add(StudentPreviewDto.builder()
                                .studentId(toInt(x.get("studentId")))
                                .studentName(strOr(x.get("studentName"), null))
                                .topicTitle(strOr(x.get("topicTitle"), null))
                                .reviewerId(toInt(x.get("reviewerId")))
                                .reviewerName(strOr(x.get("reviewerName"), null))
                                .reviewerSpecialization(strOr(x.get("reviewerSpecialization"), null))
                                .build());
                    }
                    sessions.add(SessionPreviewDto.builder()
                            .sessionId(strOr(s.get("sessionId"), null))
                            .sessionName(strOr(s.get("sessionName"), null))
                            .location(strOr(s.get("location"), null))
                            .defenseDate(parseDateTimeOrNow(s.get("defenseDate")))
                            .maxStudents(toInt(s.get("maxStudents")))
                            .virtualSession(Boolean.TRUE.equals(s.get("virtualSession")))
                            .students(students)
                            .build());
                }
                return AutoAssignPreviewResponse.builder()
                        .success(success)
                        .message(message)
                        .sessions(sessions)
                        .build();
            }

            // Fallback: simple round-robin distribution if AI is not configured
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> studentsRaw = (List<Map<String, Object>>) payload.getOrDefault("students", List.of());
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> sessionsRaw = (List<Map<String, Object>>) payload.getOrDefault("sessions", List.of());

            List<StudentPreviewDto> students = new ArrayList<>();
            for (Map<String, Object> s : studentsRaw) {
                Integer sid = toInt(s.get("studentId"));
                String name = s.get("fullName") != null ? String.valueOf(s.get("fullName")) : ("Sinh viên " + sid);
                String topicTitle = s.get("topicTitle") != null ? String.valueOf(s.get("topicTitle")) : "";
                students.add(StudentPreviewDto.builder()
                        .studentId(sid)
                        .studentName(name)
                        .topicTitle(topicTitle)
                        .build());
            }

            List<SessionPreviewDto> sessions = new ArrayList<>();
            if (sessionsRaw.isEmpty()) {
                sessions.add(SessionPreviewDto.builder()
                        .sessionId("preview-1")
                        .sessionName("Buổi bảo vệ đề xuất")
                        .location("-")
                        .defenseDate(LocalDateTime.now())
                        .maxStudents(Math.max(5, students.size()))
                        .virtualSession(true)
                        .students(students)
                        .build());
            } else {
                // Simple distribution across provided sessions (round-robin, capacity-aware)
                List<SessionPreviewDto> base = new ArrayList<>();
                for (Map<String, Object> sr : sessionsRaw) {
                    base.add(SessionPreviewDto.builder()
                            .sessionId(strOr(sr.get("sessionId"), null))
                            .sessionName(strOr(sr.get("sessionName"), "Buổi"))
                            .location(strOr(sr.get("location"), "-"))
                            .defenseDate(parseDateTimeOrNow(sr.get("defenseDate")))
                            .maxStudents(toInt(sr.get("maxStudents")))
                            .virtualSession(false)
                            .students(new ArrayList<>())
                            .build());
                }
                int i = 0;
                for (StudentPreviewDto sp : students) {
                    int attempt = 0;
                    while (attempt < base.size()) {
                        SessionPreviewDto target = base.get(i % base.size());
                        int current = target.getStudents() != null ? target.getStudents().size() : 0;
                        int cap = target.getMaxStudents() != null ? target.getMaxStudents() : 5;
                        if (current < cap) {
                            target.getStudents().add(sp);
                            break;
                        }
                        i++;
                        attempt++;
                    }
                    i++;
                }
                sessions.addAll(base);
            }

            return AutoAssignPreviewResponse.builder()
                    .success(true)
                    .sessions(sessions)
                    .message("AI preview generated (fallback)")
                    .build();
        } catch (Exception e) {
            return AutoAssignPreviewResponse.builder()
                    .success(false)
                    .message("AI error: " + e.getMessage())
                    .sessions(List.of())
                    .build();
        }
    }

    private String callGeminiRest(Map<String, Object> payload) {
        try {
            if (geminiApiKey == null || geminiApiKey.isBlank()) return null;
            String url = "https://generativelanguage.googleapis.com/v1beta/models/" + geminiModelName + ":generateContent?key=" + geminiApiKey;
            ObjectMapper mapper = new ObjectMapper();
            String payloadJson = mapper.writeValueAsString(payload);
            String instruction = "You are scheduling students into defense sessions. Return ONLY compact JSON with keys: success (boolean), message (string), sessions (array). Each session has: sessionId, sessionName, location, defenseDate (ISO), maxStudents (int), virtualSession (bool), students (array of {studentId, studentName, topicTitle, major, reviewerId, reviewerName}). Respect capacities; prefer matching reviewer specialization to student major. If cannot place, include a virtual 'unassigned' session.";

            Map<String, Object> part = new HashMap<>();
            part.put("text", instruction + "\nInput:" + payloadJson + "\nOutput JSON:");
            Map<String, Object> content = new HashMap<>();
            content.put("parts", List.of(part));
            Map<String, Object> body = new HashMap<>();
            body.put("contents", List.of(content));

            RestTemplate rt = new RestTemplate();
            @SuppressWarnings("unchecked")
            Map<String, Object> resp = rt.postForObject(url, body, Map.class);
            if (resp == null) return null;
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) resp.get("candidates");
            if (candidates == null || candidates.isEmpty()) return null;
            @SuppressWarnings("unchecked")
            Map<String, Object> contentResp = (Map<String, Object>) candidates.get(0).get("content");
            if (contentResp == null) return null;
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> parts = (List<Map<String, Object>>) contentResp.get("parts");
            if (parts == null || parts.isEmpty()) return null;
            Object text = parts.get(0).get("text");
            return text != null ? String.valueOf(text) : null;
        } catch (Exception e) {
            return null;
        }
    }

    private static Integer toInt(Object v) {
        try { return v == null ? null : Integer.valueOf(String.valueOf(v)); } catch (Exception e) { return null; }
    }
    private static String strOr(Object v, String d) { return v == null ? d : String.valueOf(v); }
    private static LocalDateTime parseDateTimeOrNow(Object v) {
        try {
            if (v == null) return LocalDateTime.now();
            if (v instanceof LocalDateTime ldt) return ldt;
            return LocalDateTime.parse(String.valueOf(v));
        } catch (Exception e) { return LocalDateTime.now(); }
    }
}
