package com.phenikaa.thesisservice.dto.request;

import com.phenikaa.thesisservice.entity.Register;
import lombok.Data;

@Data
public class RegisterTopicDTO {
    private Integer topicId;             // Đề tài muốn đăng ký
    private Integer groupId;             // Nếu là nhóm thì gửi groupId
    private String motivation;           // Lý do chọn đề tài
    private Register.RegisterType registerType;
}
