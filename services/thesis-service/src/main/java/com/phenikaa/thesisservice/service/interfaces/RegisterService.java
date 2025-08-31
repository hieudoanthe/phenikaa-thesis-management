package com.phenikaa.thesisservice.service.interfaces;

import com.phenikaa.thesisservice.dto.request.RegisterTopicRequest;

public interface RegisterService {
    void registerTopic(RegisterTopicRequest dto, Integer userId);
}
