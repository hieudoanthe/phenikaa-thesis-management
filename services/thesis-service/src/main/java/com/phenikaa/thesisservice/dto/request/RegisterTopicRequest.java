package com.phenikaa.thesisservice.dto.request;

import lombok.Data;

@Data
public class RegisterTopicRequest {
    private Integer topicId;
    private Integer registrationPeriodId; // Cho phép chỉ định đợt đăng ký cụ thể
}
