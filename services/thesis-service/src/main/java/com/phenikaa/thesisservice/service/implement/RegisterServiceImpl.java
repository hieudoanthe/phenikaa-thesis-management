package com.phenikaa.thesisservice.service.implement;

import com.phenikaa.thesisservice.dto.request.RegisterTopicDTO;
import com.phenikaa.thesisservice.entity.ProjectTopic;
import com.phenikaa.thesisservice.entity.Register;
import com.phenikaa.thesisservice.mapper.RegisterMapper;
import com.phenikaa.thesisservice.repository.ProjectTopicRepository;
import com.phenikaa.thesisservice.repository.RegisterRepository;
import com.phenikaa.thesisservice.service.interfaces.RegisterService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RegisterServiceImpl implements RegisterService {

    private final RegisterMapper registerMapper;
    private final RegisterRepository registerRepository;
    private final ProjectTopicRepository projectTopicRepository;

    @Override
    public Register registerTopic(RegisterTopicDTO dto, Integer userId) {
        Register register = registerMapper.toRegister(dto);

        ProjectTopic topic = projectTopicRepository.findById(dto.getTopicId())
                .orElseThrow(() -> new RuntimeException("Topic not found"));

        register.setProjectTopic(topic);

        register.setStudentId(userId);

        return registerRepository.save(register);
    }
}
