package com.phenikaa.submissionservice.client;

import com.phenikaa.filter.FeignTokenInterceptor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(
        name = "api-gateway",
        contextId = "notificationServiceClient",
        path = "/communication-log-service",
        configuration = FeignTokenInterceptor.class
)
public interface NotificationServiceClient {

    @PostMapping("/notifications/send")
    String sendNotification(@RequestBody Map<String, Object> notificationRequest);
}
