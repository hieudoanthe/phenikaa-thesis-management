package com.phenikaa.submissionservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "communication-log-service", url = "http://localhost:8088")
public interface CommunicationServiceClient {
    
    /**
     * Gửi thông báo qua communication-log-service
     */
    @PostMapping("/notifications/send")
    String sendNotification(@RequestBody Map<String, Object> notificationRequest);
}
