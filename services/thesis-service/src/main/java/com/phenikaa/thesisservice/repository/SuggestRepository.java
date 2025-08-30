package com.phenikaa.thesisservice.repository;

import com.phenikaa.thesisservice.entity.SuggestedTopic;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SuggestRepository extends JpaRepository<SuggestedTopic, Integer> {
    Page<SuggestedTopic> findBySuggestedBy(Integer studentId, Pageable pageable);
    
    // Kiểm tra xem sinh viên đã đề xuất trong đợt đăng ký chưa
    boolean existsBySuggestedByAndRegistrationPeriodId(Integer suggestedBy, Integer registrationPeriodId);
}
