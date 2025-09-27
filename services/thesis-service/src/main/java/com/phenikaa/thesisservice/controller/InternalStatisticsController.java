package com.phenikaa.thesisservice.controller;

import com.phenikaa.thesisservice.entity.Register;
import com.phenikaa.thesisservice.entity.SuggestedTopic;
import com.phenikaa.thesisservice.repository.RegisterRepository;
import com.phenikaa.thesisservice.repository.SuggestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// DTO class for period statistics summary
class PeriodStatisticsSummary {
    public final long totalRegistrations;
    public final long approvedRegistrations;
    public final long pendingRegistrations;
    public final long rejectedRegistrations;
    public final long totalSuggestions;
    public final long approvedSuggestions;
    public final long pendingSuggestions;
    public final long rejectedSuggestions;
    public final long totalUniqueStudents;

    private PeriodStatisticsSummary(Builder builder) {
        this.totalRegistrations = builder.totalRegistrations;
        this.approvedRegistrations = builder.approvedRegistrations;
        this.pendingRegistrations = builder.pendingRegistrations;
        this.rejectedRegistrations = builder.rejectedRegistrations;
        this.totalSuggestions = builder.totalSuggestions;
        this.approvedSuggestions = builder.approvedSuggestions;
        this.pendingSuggestions = builder.pendingSuggestions;
        this.rejectedSuggestions = builder.rejectedSuggestions;
        this.totalUniqueStudents = builder.totalUniqueStudents;
    }
    
    public static class Builder {
        private long totalRegistrations;
        private long approvedRegistrations;
        private long pendingRegistrations;
        private long rejectedRegistrations;
        private long totalSuggestions;
        private long approvedSuggestions;
        private long pendingSuggestions;
        private long rejectedSuggestions;
        private long totalUniqueStudents;
        
        public Builder setRegistrationStats(long total, long approved, long pending, long rejected) {
            this.totalRegistrations = total;
            this.approvedRegistrations = approved;
            this.pendingRegistrations = pending;
            this.rejectedRegistrations = rejected;
            return this;
        }
        
        public Builder setSuggestionStats(long total, long approved, long pending, long rejected) {
            this.totalSuggestions = total;
            this.approvedSuggestions = approved;
            this.pendingSuggestions = pending;
            this.rejectedSuggestions = rejected;
            return this;
        }
        
        public Builder setTotalUniqueStudents(long totalUniqueStudents) {
            this.totalUniqueStudents = totalUniqueStudents;
            return this;
        }
        
        public PeriodStatisticsSummary build() {
            return new PeriodStatisticsSummary(this);
        }
    }
}

@RestController
@RequestMapping("/internal/thesis")
@RequiredArgsConstructor
@Slf4j
public class InternalStatisticsController {
    
    private final RegisterRepository registerRepository;
    private final SuggestRepository suggestRepository;

    @GetMapping("/get-registered-students-count-by-period")
    public Long getRegisteredStudentsCountByPeriod(@RequestParam Integer periodId) {
        log.info("Getting registered students count by period: {}", periodId);
        return registerRepository.countByRegistrationPeriodId(periodId);
    }

    @GetMapping("/get-registered-students-by-period")
    public List<Register> getRegisteredStudentsByPeriod(@RequestParam Integer periodId) {
        log.info("Getting registered students by period: {}", periodId);
        return registerRepository.findByRegistrationPeriodId(periodId);
    }

    @GetMapping("/get-approved-students-count-by-period")
    public Long getApprovedStudentsCountByPeriod(@RequestParam Integer periodId) {
        log.info("Getting approved students count by period: {}", periodId);
        return registerRepository.findByRegistrationPeriodId(periodId)
            .stream()
            .filter(register -> register.getRegisterStatus() == Register.RegisterStatus.APPROVED)
            .count();
    }

    @GetMapping("/get-pending-students-count-by-period")
    public Long getPendingStudentsCountByPeriod(@RequestParam Integer periodId) {
        log.info("Getting pending students count by period: {}", periodId);
        return registerRepository.findByRegistrationPeriodId(periodId)
            .stream()
            .filter(register -> register.getRegisterStatus() == Register.RegisterStatus.PENDING)
            .count();
    }

    @GetMapping("/get-rejected-students-count-by-period")
    public Long getRejectedStudentsCountByPeriod(@RequestParam Integer periodId) {
        log.info("Getting rejected students count by period: {}", periodId);
        return registerRepository.findByRegistrationPeriodId(periodId)
            .stream()
            .filter(register -> register.getRegisterStatus() == Register.RegisterStatus.REJECTED)
            .count();
    }

    // Suggested Topics Statistics
    @GetMapping("/get-suggested-topics-count-by-period")
    public Long getSuggestedTopicsCountByPeriod(@RequestParam Integer periodId) {
        log.info("Getting suggested topics count by period: {}", periodId);
        return (long) suggestRepository.findByRegistrationPeriodId(periodId).size();
    }

    @GetMapping("/get-suggested-topics-by-period")
    public List<SuggestedTopic> getSuggestedTopicsByPeriod(@RequestParam Integer periodId) {
        log.info("Getting suggested topics by period: {}", periodId);
        return suggestRepository.findByRegistrationPeriodId(periodId);
    }

    @GetMapping("/get-approved-suggestions-count-by-period")
    public Long getApprovedSuggestionsCountByPeriod(@RequestParam Integer periodId) {
        log.info("Getting approved suggestions count by period: {}", periodId);
        return suggestRepository.findByRegistrationPeriodId(periodId)
            .stream()
            .filter(suggestion -> suggestion.getSuggestionStatus() == SuggestedTopic.SuggestionStatus.APPROVED)
            .count();
    }

    @GetMapping("/get-pending-suggestions-count-by-period")
    public Long getPendingSuggestionsCountByPeriod(@RequestParam Integer periodId) {
        log.info("Getting pending suggestions count by period: {}", periodId);
        return suggestRepository.findByRegistrationPeriodId(periodId)
            .stream()
            .filter(suggestion -> suggestion.getSuggestionStatus() == SuggestedTopic.SuggestionStatus.PENDING)
            .count();
    }

    @GetMapping("/get-rejected-suggestions-count-by-period")
    public Long getRejectedSuggestionsCountByPeriod(@RequestParam Integer periodId) {
        log.info("Getting rejected suggestions count by period: {}", periodId);
        return suggestRepository.findByRegistrationPeriodId(periodId)
            .stream()
            .filter(suggestion -> suggestion.getSuggestionStatus() == SuggestedTopic.SuggestionStatus.REJECTED)
            .count();
    }

    // Combined Statistics - Total students involved in period (suggestions + registrations)
    @GetMapping("/get-total-students-involved-by-period")
    public Long getTotalStudentsInvolvedByPeriod(@RequestParam Integer periodId) {
        log.info("Getting total students involved by period: {}", periodId);
        
        // Get unique student IDs from suggestions
        List<Integer> suggestionStudentIds = suggestRepository.findByRegistrationPeriodId(periodId)
            .stream()
            .map(SuggestedTopic::getSuggestedBy)
            .distinct()
            .toList();
        
        // Get unique student IDs from registrations
        List<Integer> registrationStudentIds = registerRepository.findByRegistrationPeriodId(periodId)
            .stream()
            .map(Register::getStudentId)
            .distinct()
            .toList();
        
        // Combine and get unique count
        return suggestionStudentIds.stream()
            .filter(registrationStudentIds::contains)
            .count() + // Students who both suggested and registered
            suggestionStudentIds.stream()
            .filter(id -> !registrationStudentIds.contains(id))
            .count() + // Students who only suggested
            registrationStudentIds.stream()
            .filter(id -> !suggestionStudentIds.contains(id))
            .count(); // Students who only registered
    }

    @GetMapping("/get-period-statistics-summary")
    public Object getPeriodStatisticsSummary(@RequestParam Integer periodId) {
        log.info("Getting period statistics summary for period: {}", periodId);
        
        // Get all data for the period
        List<Register> registrations = registerRepository.findByRegistrationPeriodId(periodId);
        List<SuggestedTopic> suggestions = suggestRepository.findByRegistrationPeriodId(periodId);
        
        // Calculate statistics
        long totalRegistrations = registrations.size();
        long approvedRegistrations = registrations.stream()
            .filter(r -> r.getRegisterStatus() == Register.RegisterStatus.APPROVED)
            .count();
        long pendingRegistrations = registrations.stream()
            .filter(r -> r.getRegisterStatus() == Register.RegisterStatus.PENDING)
            .count();
        long rejectedRegistrations = registrations.stream()
            .filter(r -> r.getRegisterStatus() == Register.RegisterStatus.REJECTED)
            .count();
        
        long totalSuggestions = suggestions.size();
        long approvedSuggestions = suggestions.stream()
            .filter(s -> s.getSuggestionStatus() == SuggestedTopic.SuggestionStatus.APPROVED)
            .count();
        long pendingSuggestions = suggestions.stream()
            .filter(s -> s.getSuggestionStatus() == SuggestedTopic.SuggestionStatus.PENDING)
            .count();
        long rejectedSuggestions = suggestions.stream()
            .filter(s -> s.getSuggestionStatus() == SuggestedTopic.SuggestionStatus.REJECTED)
            .count();
        
        // Get unique students
        List<Integer> suggestionStudentIds = suggestions.stream()
            .map(SuggestedTopic::getSuggestedBy)
            .distinct()
            .toList();
        List<Integer> registrationStudentIds = registrations.stream()
            .map(Register::getStudentId)
            .distinct()
            .toList();
        
        long totalUniqueStudents = suggestionStudentIds.stream()
            .filter(registrationStudentIds::contains)
            .count() + // Students who both suggested and registered
            suggestionStudentIds.stream()
            .filter(id -> !registrationStudentIds.contains(id))
            .count() + // Students who only suggested
            registrationStudentIds.stream()
            .filter(id -> !suggestionStudentIds.contains(id))
            .count(); // Students who only registered
        
        // Create a proper response object to avoid circular reference
        return new PeriodStatisticsSummary.Builder()
            .setRegistrationStats(totalRegistrations, approvedRegistrations, pendingRegistrations, rejectedRegistrations)
            .setSuggestionStats(totalSuggestions, approvedSuggestions, pendingSuggestions, rejectedSuggestions)
            .setTotalUniqueStudents(totalUniqueStudents)
            .build();
    }
}
