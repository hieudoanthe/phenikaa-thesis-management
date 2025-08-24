package com.phenikaa.userservice.dto.response;

import com.phenikaa.dto.response.GetUserResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserFilterResponse {
    
    private List<GetUserResponse> users;
    private int currentPage;
    private int totalPages;
    private long totalElements;
    private int pageSize;
    private boolean hasNext;
    private boolean hasPrevious;
    
    /**
     * Tạo response từ Page object
     */
    public static UserFilterResponse fromPage(Page<GetUserResponse> page) {
        return new UserFilterResponse(
            page.getContent(),
            page.getNumber(),
            page.getTotalPages(),
            page.getTotalElements(),
            page.getSize(),
            page.hasNext(),
            page.hasPrevious()
        );
    }
}
