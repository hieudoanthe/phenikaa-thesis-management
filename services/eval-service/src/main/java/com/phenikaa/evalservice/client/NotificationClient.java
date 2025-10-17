package com.phenikaa.evalservice.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@FeignClient(
        name = "api-gateway",
        contextId = "notificationServiceClient",
        path = "/communication-log-service",
        configuration = FeignTokenInterceptor.class
)
public interface NotificationServiceClient {
    @PostMapping("/notifications/send")
    void sendNotification(@RequestBody NotificationRequest noti);
}


