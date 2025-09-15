package com.phenikaa.thesisservice.dto.request;

import lombok.*;

@NoArgsConstructor
@Getter
@Setter
public class NotificationRequest {
    private Integer senderId;
    private Integer receiverId;
    private String message;
    private String type;

    public NotificationRequest(Integer senderId, Integer receiverId, String message) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.message = message;
        this.type = null;
    }

    public NotificationRequest(Integer senderId, Integer receiverId, String message, String type) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.message = message;
        this.type = type;
    }
}
