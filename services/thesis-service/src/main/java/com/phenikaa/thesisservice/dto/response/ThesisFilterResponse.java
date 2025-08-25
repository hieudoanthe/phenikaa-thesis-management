package com.phenikaa.thesisservice.dto.response;

import lombok.Data;
import lombok.Builder;

import java.util.List;

@Data
@Builder
public class ThesisFilterResponse {
    
    private List<GetThesisResponse> content;
    
    // Thông tin phân trang
    private Integer currentPage;
    private Integer totalPages;
    private Long totalElements;
    private Integer pageSize;
    private Boolean hasNext;
    private Boolean hasPrevious;
    
    // Thống kê
    private Long totalTopics;
    private Long availableTopics;
    private Long approvedTopics;
    private Long pendingTopics;
    
    /**
     * Tạo response từ Page object
     */
    public static ThesisFilterResponse fromPage(org.springframework.data.domain.Page<GetThesisResponse> page) {
        return ThesisFilterResponse.builder()
                .content(page.getContent())
                .currentPage(page.getNumber())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .pageSize(page.getSize())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();
    }
    
    /**
     * Tạo response với thống kê
     */
    public static ThesisFilterResponse fromPageWithStats(
            org.springframework.data.domain.Page<GetThesisResponse> page,
            Long totalTopics,
            Long availableTopics,
            Long approvedTopics,
            Long pendingTopics) {
        
        ThesisFilterResponse response = fromPage(page);
        response.setTotalTopics(totalTopics);
        response.setAvailableTopics(availableTopics);
        response.setApprovedTopics(approvedTopics);
        response.setPendingTopics(pendingTopics);
        
        return response;
    }
}
