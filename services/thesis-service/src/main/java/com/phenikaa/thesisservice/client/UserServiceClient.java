package com.phenikaa.thesisservice.client;

import com.phenikaa.common.dto.UserDto;
import com.phenikaa.thesisservice.config.FeignTokenInterceptor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service",  configuration = FeignTokenInterceptor.class)
public interface UserServiceClient {

    @GetMapping("/{id}")
    UserDto getUserById(@PathVariable("id") Integer id);

}
