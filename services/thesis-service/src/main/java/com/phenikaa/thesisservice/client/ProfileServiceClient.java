package com.phenikaa.thesisservice.client;

import com.phenikaa.common.dto.ProfileDto;
import com.phenikaa.thesisservice.config.FeignTokenInterceptor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "profile-service", configuration = FeignTokenInterceptor.class)
public interface ProfileServiceClient {

    @GetMapping("/{id}")
    ProfileDto getSupervisorId(@PathVariable("id") Integer id);

    @GetMapping("/get-name")
    String getFullNameByUserId(@RequestParam("userId") Integer userId);

}
