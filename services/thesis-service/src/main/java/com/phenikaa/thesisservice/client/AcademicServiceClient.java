package com.phenikaa.thesisservice.client;

import com.phenikaa.dto.response.GetAcademicResponse;
import com.phenikaa.filter.FeignTokenInterceptor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "academic-service", configuration = FeignTokenInterceptor.class)
public interface AcademicServiceClient {
    @GetMapping("/academicId/")
    GetAcademicResponse getYearName();
}
