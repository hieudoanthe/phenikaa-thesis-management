package com.phenikaa.thesisservice.repository;

import com.phenikaa.thesisservice.entity.SuggestedTopic;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SuggestRepository extends JpaRepository<SuggestedTopic, Integer> {
    Page<SuggestedTopic> findBySuggestedBy(Integer studentId, Pageable pageable);
    // Kiểm tra xem sinh viên đã đề xuất trong đợt đăng ký chưa
    boolean existsBySuggestedByAndRegistrationPeriodId(Integer suggestedBy, Integer registrationPeriodId);
    
    // Tìm tất cả đề tài đã được approved với phân trang
    Page<SuggestedTopic> findBySuggestionStatus(SuggestedTopic.SuggestionStatus status, Pageable pageable);
    
    // Lấy danh sách đề tài được đề xuất theo đợt đăng ký
    List<SuggestedTopic> findByRegistrationPeriodId(Integer registrationPeriodId);
}
