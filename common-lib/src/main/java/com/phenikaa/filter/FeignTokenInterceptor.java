package com.phenikaa.filter;

import com.phenikaa.utils.JwtUtil;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class FeignTokenInterceptor implements RequestInterceptor {

    private final JwtUtil jwtUtil;

    @Override
    public void apply(RequestTemplate template) {
        String token = null;
        
        try {
            // Thử lấy token từ RequestContext trước (cho sync calls)
            if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes reqAttr) {
                token = reqAttr.getRequest().getHeader("Authorization");
                log.debug("Using token from RequestContext for: {}", template.url());
            }
        } catch (Exception e) {
            log.debug("No RequestContext available: {}", e.getMessage());
        }
        
        // Nếu không có token từ RequestContext, thử lấy từ SecurityContext
        if (token == null) {
            try {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication != null && authentication.getCredentials() instanceof String authToken) {
                    token = "Bearer " + authToken;
                    log.debug("Using token from SecurityContext for: {}", template.url());
                }
            } catch (Exception e) {
                log.debug("No SecurityContext available: {}", e.getMessage());
            }
        }
        
        // Nếu vẫn không có token, tạo internal service token
        if (token == null) {
            token = "Bearer " + jwtUtil.generateInternalServiceToken();
            log.debug("Using internal service token for: {}", template.url());
        }

        template.header("Authorization", token);
    }
}
