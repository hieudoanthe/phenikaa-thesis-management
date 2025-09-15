package com.phenikaa.evalservice.service;

import com.phenikaa.evalservice.dto.response.*;
import com.phenikaa.evalservice.entity.*;
import com.phenikaa.evalservice.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class StatisticsService {
    
    private final DefenseSessionRepository defenseSessionRepository;
    private final StudentDefenseRepository studentDefenseRepository;
    private final ProjectEvaluationRepository evaluationRepository;
    private final DefenseCommitteeRepository committeeRepository;
    private final DefenseQnARepository qnARepository;
    
    /**
     * Lấy thống kê tổng quan
     */
    public StatisticsOverviewResponse getOverviewStatistics() {
        log.info("Calculating overview statistics");
        
        // Thống kê buổi bảo vệ
        long totalSessions = defenseSessionRepository.count();
        long scheduledSessions = defenseSessionRepository.findByStatus(DefenseSession.SessionStatus.SCHEDULED).size();
        long inProgressSessions = defenseSessionRepository.findByStatus(DefenseSession.SessionStatus.IN_PROGRESS).size();
        long completedSessions = defenseSessionRepository.findByStatus(DefenseSession.SessionStatus.COMPLETED).size();
        long cancelledSessions = defenseSessionRepository.findByStatus(DefenseSession.SessionStatus.CANCELLED).size();
        
        // Thống kê sinh viên
        long totalStudents = studentDefenseRepository.count();
        long pendingStudents = studentDefenseRepository.findByStatus(StudentDefense.DefenseStatus.SCHEDULED).size();
        long completedStudents = studentDefenseRepository.findByStatus(StudentDefense.DefenseStatus.COMPLETED).size();
        long failedStudents = 0; // TODO: Thêm trạng thái FAILED nếu cần
        
        // Thống kê đánh giá
        long totalEvaluations = evaluationRepository.count();
        List<ProjectEvaluation> allEvaluations = evaluationRepository.findAll();
        long supervisorEvaluations = allEvaluations.stream()
                .mapToLong(eval -> eval.getEvaluationType() == ProjectEvaluation.EvaluationType.SUPERVISOR ? 1 : 0)
                .sum();
        long reviewerEvaluations = allEvaluations.stream()
                .mapToLong(eval -> eval.getEvaluationType() == ProjectEvaluation.EvaluationType.REVIEWER ? 1 : 0)
                .sum();
        long committeeEvaluations = allEvaluations.stream()
                .mapToLong(eval -> eval.getEvaluationType() == ProjectEvaluation.EvaluationType.COMMITTEE ? 1 : 0)
                .sum();
        
        // Thống kê Q&A
        long totalQnAs = qnARepository.count();
        
        // Điểm trung bình
        Double averageScore = allEvaluations.stream()
                .filter(eval -> eval.getTotalScore() != null)
                .mapToDouble(ProjectEvaluation::getTotalScore)
                .average()
                .orElse(0.0);
        
        // Tỷ lệ hoàn thành
        double completionRate = totalStudents > 0 ? (double) completedStudents / totalStudents * 100 : 0.0;
        
        return StatisticsOverviewResponse.builder()
                .totalDefenseSessions(totalSessions)
                .scheduledSessions(scheduledSessions)
                .inProgressSessions(inProgressSessions)
                .completedSessions(completedSessions)
                .cancelledSessions(cancelledSessions)
                .totalStudents(totalStudents)
                .pendingStudents(pendingStudents)
                .completedStudents(completedStudents)
                .failedStudents(failedStudents)
                .totalEvaluations(totalEvaluations)
                .supervisorEvaluations(supervisorEvaluations)
                .reviewerEvaluations(reviewerEvaluations)
                .committeeEvaluations(committeeEvaluations)
                .totalQnAs(totalQnAs)
                .averageScore(averageScore)
                .completionRate(completionRate)
                .build();
    }
    
    /**
     * Lấy thống kê buổi bảo vệ
     */
    public DefenseStatisticsResponse getDefenseStatistics(LocalDate startDate, LocalDate endDate) {
        log.info("Calculating defense statistics from {} to {}", startDate, endDate);
        
        List<DefenseSession> sessions = defenseSessionRepository.findAll();
        
        // Lọc theo ngày nếu có
        if (startDate != null && endDate != null) {
            sessions = sessions.stream()
                    .filter(session -> {
                        LocalDate sessionDate = session.getDefenseDate();
                        return sessionDate != null && 
                               !sessionDate.isBefore(startDate) && 
                               !sessionDate.isAfter(endDate);
                    })
                    .collect(Collectors.toList());
        }
        
        // Thống kê theo trạng thái
        Map<String, Long> statusCounts = sessions.stream()
                .collect(Collectors.groupingBy(
                        session -> session.getStatus().toString(),
                        Collectors.counting()
                ));
        
        // Thống kê theo tháng
        List<DefenseStatisticsResponse.MonthlyDefenseData> monthlyData = sessions.stream()
                .filter(session -> session.getDefenseDate() != null)
                .collect(Collectors.groupingBy(
                        session -> session.getDefenseDate().format(DateTimeFormatter.ofPattern("yyyy-MM")),
                        Collectors.toList()
                ))
                .entrySet().stream()
                .map(entry -> {
                    List<DefenseSession> monthSessions = entry.getValue();
                    long total = monthSessions.size();
                    long completed = monthSessions.stream()
                            .mapToLong(s -> s.getStatus() == DefenseSession.SessionStatus.COMPLETED ? 1 : 0)
                            .sum();
                    long pending = monthSessions.stream()
                            .mapToLong(s -> s.getStatus() == DefenseSession.SessionStatus.SCHEDULED ? 1 : 0)
                            .sum();
                    
                    return DefenseStatisticsResponse.MonthlyDefenseData.builder()
                            .month(entry.getKey())
                            .count((long) total)
                            .completed(completed)
                            .pending(pending)
                            .build();
                })
                .sorted(Comparator.comparing(DefenseStatisticsResponse.MonthlyDefenseData::getMonth))
                .collect(Collectors.toList());
        
        // Thống kê theo phòng
        Map<String, Long> roomCounts = sessions.stream()
                .filter(session -> session.getLocation() != null)
                .collect(Collectors.groupingBy(
                        DefenseSession::getLocation,
                        Collectors.counting()
                ));
        
        // Thống kê theo giảng viên
        List<DefenseStatisticsResponse.LecturerDefenseData> lecturerData = new ArrayList<>();
        // TODO: Implement lecturer statistics
        
        // Thống kê thời gian
        LocalDate today = LocalDate.now();
        long todaySessions = sessions.stream()
                .mapToLong(s -> s.getDefenseDate() != null && s.getDefenseDate().equals(today) ? 1 : 0)
                .sum();
        
        LocalDate weekStart = today.minusDays(7);
        long weekSessions = sessions.stream()
                .mapToLong(s -> s.getDefenseDate() != null && 
                               !s.getDefenseDate().isBefore(weekStart) && 
                               !s.getDefenseDate().isAfter(today) ? 1 : 0)
                .sum();
        
        LocalDate monthStart = today.minusDays(30);
        long monthSessions = sessions.stream()
                .mapToLong(s -> s.getDefenseDate() != null && 
                               !s.getDefenseDate().isBefore(monthStart) && 
                               !s.getDefenseDate().isAfter(today) ? 1 : 0)
                .sum();
        
        return DefenseStatisticsResponse.builder()
                .statusCounts(statusCounts)
                .monthlyData(monthlyData)
                .roomCounts(roomCounts)
                .lecturerData(lecturerData)
                .averageDuration(0.0) // TODO: Tính thời gian trung bình
                .todaySessions(todaySessions)
                .weekSessions(weekSessions)
                .monthSessions(monthSessions)
                .build();
    }
    
    /**
     * Lấy thống kê đánh giá
     */
    public EvaluationStatisticsResponse getEvaluationStatistics(LocalDate startDate, LocalDate endDate) {
        log.info("Calculating evaluation statistics from {} to {}", startDate, endDate);
        
        List<ProjectEvaluation> evaluations = evaluationRepository.findAll();
        
        // Lọc theo ngày nếu có
        if (startDate != null && endDate != null) {
            evaluations = evaluations.stream()
                    .filter(eval -> {
                        LocalDateTime evalDate = eval.getEvaluatedAt();
                        return evalDate != null && 
                               !evalDate.toLocalDate().isBefore(startDate) && 
                               !evalDate.toLocalDate().isAfter(endDate);
                    })
                    .collect(Collectors.toList());
        }
        
        // Tổng số đánh giá
        long totalEvaluations = evaluations.size();
        
        // Số đánh giá theo loại
        Map<String, Long> typeCounts = evaluations.stream()
                .collect(Collectors.groupingBy(
                        eval -> eval.getEvaluationType().toString(),
                        Collectors.counting()
                ));
        
        // Số đánh giá theo trạng thái
        Map<String, Long> statusCounts = evaluations.stream()
                .collect(Collectors.groupingBy(
                        eval -> eval.getEvaluationStatus() != null ? eval.getEvaluationStatus().toString() : "UNKNOWN",
                        Collectors.counting()
                ));
        
        // Thống kê theo tháng
        List<EvaluationStatisticsResponse.MonthlyEvaluationData> monthlyData = evaluations.stream()
                .filter(eval -> eval.getEvaluatedAt() != null)
                .collect(Collectors.groupingBy(
                        eval -> eval.getEvaluatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM")),
                        Collectors.toList()
                ))
                .entrySet().stream()
                .map(entry -> {
                    List<ProjectEvaluation> monthEvaluations = entry.getValue();
                    long total = monthEvaluations.size();
                    long supervisor = monthEvaluations.stream()
                            .mapToLong(e -> e.getEvaluationType() == ProjectEvaluation.EvaluationType.SUPERVISOR ? 1 : 0)
                            .sum();
                    long reviewer = monthEvaluations.stream()
                            .mapToLong(e -> e.getEvaluationType() == ProjectEvaluation.EvaluationType.REVIEWER ? 1 : 0)
                            .sum();
                    long committee = monthEvaluations.stream()
                            .mapToLong(e -> e.getEvaluationType() == ProjectEvaluation.EvaluationType.COMMITTEE ? 1 : 0)
                            .sum();
                    
                    return EvaluationStatisticsResponse.MonthlyEvaluationData.builder()
                            .month(entry.getKey())
                            .total(total)
                            .supervisor(supervisor)
                            .reviewer(reviewer)
                            .committee(committee)
                            .build();
                })
                .sorted(Comparator.comparing(EvaluationStatisticsResponse.MonthlyEvaluationData::getMonth))
                .collect(Collectors.toList());
        
        // Thống kê theo giảng viên
        List<EvaluationStatisticsResponse.LecturerEvaluationData> lecturerData = evaluations.stream()
                .collect(Collectors.groupingBy(
                        ProjectEvaluation::getEvaluatorId,
                        Collectors.toList()
                ))
                .entrySet().stream()
                .map(entry -> {
                    List<ProjectEvaluation> lecturerEvaluations = entry.getValue();
                    long count = lecturerEvaluations.size();
                    double averageScore = lecturerEvaluations.stream()
                            .filter(eval -> eval.getTotalScore() != null)
                            .mapToDouble(ProjectEvaluation::getTotalScore)
                            .average()
                            .orElse(0.0);
                    
                    String mostCommonType = lecturerEvaluations.stream()
                            .collect(Collectors.groupingBy(
                                    ProjectEvaluation::getEvaluationType,
                                    Collectors.counting()
                            ))
                            .entrySet().stream()
                            .max(Map.Entry.comparingByValue())
                            .map(entry2 -> entry2.getKey().toString())
                            .orElse("UNKNOWN");
                    
                    return EvaluationStatisticsResponse.LecturerEvaluationData.builder()
                            .lecturerId(entry.getKey())
                            .lecturerName("Lecturer " + entry.getKey()) // TODO: Lấy tên thật
                            .evaluationCount(count)
                            .averageScore(averageScore)
                            .mostCommonType(mostCommonType)
                            .build();
                })
                .collect(Collectors.toList());
        
        // Tỷ lệ hoàn thành
        long completedCount = evaluations.stream()
                .mapToLong(eval -> eval.getEvaluationStatus() == ProjectEvaluation.EvaluationStatus.COMPLETED ? 1 : 0)
                .sum();
        double completionRate = totalEvaluations > 0 ? (double) completedCount / totalEvaluations * 100 : 0.0;
        
        return EvaluationStatisticsResponse.builder()
                .totalEvaluations(totalEvaluations)
                .typeCounts(typeCounts)
                .statusCounts(statusCounts)
                .monthlyData(monthlyData)
                .lecturerData(lecturerData)
                .completionRate(completionRate)
                .averageEvaluationTime(0.0) // TODO: Tính thời gian đánh giá trung bình
                .build();
    }
    
    /**
     * Lấy thống kê điểm số
     */
    public ScoreStatisticsResponse getScoreStatistics(LocalDate startDate, LocalDate endDate) {
        log.info("Calculating score statistics from {} to {}", startDate, endDate);
        
        List<ProjectEvaluation> evaluations = evaluationRepository.findAll();
        
        // Lọc theo ngày nếu có
        if (startDate != null && endDate != null) {
            evaluations = evaluations.stream()
                    .filter(eval -> {
                        LocalDateTime evalDate = eval.getEvaluatedAt();
                        return evalDate != null && 
                               !evalDate.toLocalDate().isBefore(startDate) && 
                               !evalDate.toLocalDate().isAfter(endDate);
                    })
                    .collect(Collectors.toList());
        }
        
        // Lọc chỉ những đánh giá có điểm
        List<ProjectEvaluation> scoredEvaluations = evaluations.stream()
                .filter(eval -> eval.getTotalScore() != null)
                .collect(Collectors.toList());
        
        // Điểm trung bình tổng
        double overallAverage = scoredEvaluations.stream()
                .mapToDouble(ProjectEvaluation::getTotalScore)
                .average()
                .orElse(0.0);
        
        // Điểm trung bình theo loại
        Map<String, Double> averageByType = scoredEvaluations.stream()
                .collect(Collectors.groupingBy(
                        eval -> eval.getEvaluationType().toString(),
                        Collectors.averagingDouble(ProjectEvaluation::getTotalScore)
                ));
        
        // Phân bố điểm
        Map<String, Long> scoreDistribution = scoredEvaluations.stream()
                .collect(Collectors.groupingBy(
                        eval -> {
                            double score = eval.getTotalScore();
                            if (score >= 9) return "9-10";
                            else if (score >= 8) return "8-9";
                            else if (score >= 7) return "7-8";
                            else if (score >= 6) return "6-7";
                            else if (score >= 5) return "5-6";
                            else return "0-5";
                        },
                        Collectors.counting()
                ));
        
        // Thống kê theo tháng
        List<ScoreStatisticsResponse.MonthlyScoreData> monthlyData = scoredEvaluations.stream()
                .filter(eval -> eval.getEvaluatedAt() != null)
                .collect(Collectors.groupingBy(
                        eval -> eval.getEvaluatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM")),
                        Collectors.toList()
                ))
                .entrySet().stream()
                .map(entry -> {
                    List<ProjectEvaluation> monthEvaluations = entry.getValue();
                    long count = monthEvaluations.size();
                    double average = monthEvaluations.stream()
                            .mapToDouble(ProjectEvaluation::getTotalScore)
                            .average()
                            .orElse(0.0);
                    double highest = monthEvaluations.stream()
                            .mapToDouble(ProjectEvaluation::getTotalScore)
                            .max()
                            .orElse(0.0);
                    double lowest = monthEvaluations.stream()
                            .mapToDouble(ProjectEvaluation::getTotalScore)
                            .min()
                            .orElse(0.0);
                    
                    return ScoreStatisticsResponse.MonthlyScoreData.builder()
                            .month(entry.getKey())
                            .average(average)
                            .count(count)
                            .highest(highest)
                            .lowest(lowest)
                            .build();
                })
                .sorted(Comparator.comparing(ScoreStatisticsResponse.MonthlyScoreData::getMonth))
                .collect(Collectors.toList());
        
        // Top điểm cao nhất
        List<ScoreStatisticsResponse.TopScoreData> topScores = scoredEvaluations.stream()
                .sorted((e1, e2) -> Double.compare(e2.getTotalScore(), e1.getTotalScore()))
                .limit(10)
                .map(eval -> ScoreStatisticsResponse.TopScoreData.builder()
                        .topicId(eval.getTopicId())
                        .topicTitle("Topic " + eval.getTopicId()) // TODO: Lấy tên thật
                        .studentName("Student " + eval.getStudentId()) // TODO: Lấy tên thật
                        .score(eval.getTotalScore().doubleValue())
                        .evaluationType(eval.getEvaluationType().toString())
                        .build())
                .collect(Collectors.toList());
        
        // Tỷ lệ đạt/không đạt (giả sử >= 5 là đạt)
        long passCount = scoredEvaluations.stream()
                .mapToLong(eval -> eval.getTotalScore() >= 5.0 ? 1 : 0)
                .sum();
        double passRate = scoredEvaluations.size() > 0 ? (double) passCount / scoredEvaluations.size() * 100 : 0.0;
        
        return ScoreStatisticsResponse.builder()
                .overallAverage(overallAverage)
                .averageByType(averageByType)
                .scoreDistribution(scoreDistribution)
                .monthlyData(monthlyData)
                .topScores(topScores)
                .majorData(new ArrayList<>()) // TODO: Thêm thống kê theo chuyên ngành
                .lecturerData(new ArrayList<>()) // TODO: Thêm thống kê theo giảng viên
                .passRate(passRate)
                .averageByDifficulty(new HashMap<>()) // TODO: Thêm thống kê theo mức độ khó
                .build();
    }
    
    /**
     * Lấy thống kê theo tháng
     */
    public List<Object> getMonthlyStatistics(Integer year) {
        log.info("Getting monthly statistics for year: {}", year);
        
        // TODO: Implement monthly statistics
        return new ArrayList<>();
    }

    // Statistics methods for internal API
    public Long getEvaluationCount() {
        return evaluationRepository.count();
    }

    public Long getEvaluationCountByType(String type) {
        try {
            ProjectEvaluation.EvaluationType evaluationType = ProjectEvaluation.EvaluationType.valueOf(type.toUpperCase());
            return evaluationRepository.countByEvaluationType(evaluationType);
        } catch (IllegalArgumentException e) {
            return 0L;
        }
    }

    public Long getEvaluationCountByStatus(String status) {
        try {
            ProjectEvaluation.EvaluationStatus evaluationStatus = ProjectEvaluation.EvaluationStatus.valueOf(status.toUpperCase());
            return evaluationRepository.countByEvaluationStatus(evaluationStatus);
        } catch (IllegalArgumentException e) {
            return 0L;
        }
    }

    public Long getEvaluationCountByEvaluator(Integer evaluatorId) {
        return evaluationRepository.countByEvaluatorId(evaluatorId);
    }

    public List<Map<String, Object>> getEvaluationsOverTime(String startDate, String endDate) {
        // TODO: Implement evaluations over time with date filtering
        return new ArrayList<>();
    }

    public List<Map<String, Object>> getEvaluationsByEvaluator(Integer evaluatorId) {
        // TODO: Implement evaluations by evaluator
        return new ArrayList<>();
    }

    public List<Map<String, Object>> getEvaluationsByTopic(Integer topicId) {
        // TODO: Implement evaluations by topic
        return new ArrayList<>();
    }

    public List<Map<String, Object>> getEvaluationsByStudent(Integer studentId) {
        // TODO: Implement evaluations by student
        return new ArrayList<>();
    }

    public Map<String, Object> getScoreStatistics(String startDate, String endDate) {
        // TODO: Implement score statistics with date filtering
        Map<String, Object> stats = new HashMap<>();
        stats.put("averageScore", 0.0);
        stats.put("highestScore", 0.0);
        stats.put("lowestScore", 0.0);
        stats.put("scoreDistribution", new HashMap<String, Long>());
        stats.put("passRate", 0.0);
        return stats;
    }

    public Long getPendingEvaluations() {
        return evaluationRepository.countByEvaluationStatus(ProjectEvaluation.EvaluationStatus.PENDING);
    }

    public Long getPendingEvaluationsByEvaluator(Integer evaluatorId) {
        return evaluationRepository.countByEvaluatorIdAndEvaluationStatus(evaluatorId, ProjectEvaluation.EvaluationStatus.PENDING);
    }
    
    public List<Map<String, Object>> getPendingEvaluationsList() {
        List<ProjectEvaluation> pendingEvaluations = evaluationRepository.findByEvaluationStatus(ProjectEvaluation.EvaluationStatus.PENDING);
        
        return pendingEvaluations.stream()
                .map(evaluation -> {
                    Map<String, Object> evaluationData = new HashMap<>();
                    evaluationData.put("id", evaluation.getEvaluatorId());
                    evaluationData.put("studentId", evaluation.getStudentId());
                    evaluationData.put("topicId", evaluation.getTopicId());
                    evaluationData.put("evaluatorId", evaluation.getEvaluatorId());
                    evaluationData.put("evaluationType", evaluation.getEvaluationType());
                    evaluationData.put("status", evaluation.getEvaluationStatus());
                    evaluationData.put("createdAt", evaluation.getCreatedAt());
                    evaluationData.put("dueDate", evaluation.getEvaluatedAt());
                    return evaluationData;
                })
                .collect(Collectors.toList());
    }
}
