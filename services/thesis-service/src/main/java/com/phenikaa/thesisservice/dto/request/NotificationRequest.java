package com.phenikaa.thesisservice.dto.request;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class NotificationRequest {
    private Integer senderId;
    private Integer receiverId;
    private String message;
}
