package com.phenikaa.submissionservice.client;

import com.phenikaa.filter.FeignTokenInterceptor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(
        name = "api-gateway",
        contextId = "thesisServiceClient",
        path = "/thesis-service",
        configuration = FeignTokenInterceptor.class
)
public interface ThesisServiceClient {
    
    @GetMapping("/internal/thesis/topics/{topicId}")
    Map<String, Object> getTopicById(@PathVariable("topicId") Integer topicId);
}
