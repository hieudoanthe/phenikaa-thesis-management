package com.phenikaa.filter;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
public class FeignTokenInterceptor implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate template) {
        // Lấy token từ SecurityContext hoặc RequestContext
        String token = RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes reqAttr
                ? reqAttr.getRequest().getHeader("Authorization")
                : null;

        if (token != null) {
            template.header("Authorization", token);
        }
    }
}
