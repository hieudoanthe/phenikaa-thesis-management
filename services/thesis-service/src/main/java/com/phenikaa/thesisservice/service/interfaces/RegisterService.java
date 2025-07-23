package com.phenikaa.thesisservice.service.interfaces;

import com.phenikaa.thesisservice.dto.request.RegisterTopicDTO;
import com.phenikaa.thesisservice.entity.Register;

public interface RegisterService {
    Register registerTopic(RegisterTopicDTO dto, Integer userId);
}
