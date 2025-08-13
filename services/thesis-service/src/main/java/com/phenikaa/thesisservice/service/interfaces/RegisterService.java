package com.phenikaa.thesisservice.service.interfaces;

import com.phenikaa.thesisservice.dto.request.RegisterTopicRequest;
import com.phenikaa.thesisservice.entity.Register;

public interface RegisterService {
    Register registerTopic(RegisterTopicRequest dto, Integer userId);

}
