package com.phenikaa.thesisservice.client;

import com.phenikaa.filter.FeignTokenInterceptor;
import com.phenikaa.thesisservice.dto.request.NotificationRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "api-gateway", path = "/communication-log-service", configuration = FeignTokenInterceptor.class )
public interface NotificationServiceClient {
    @PostMapping("/notifications/send")
    void sendNotification(@RequestBody NotificationRequest noti);
}
