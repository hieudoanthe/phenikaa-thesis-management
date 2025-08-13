package com.phenikaa.thesisservice.service.implement;

import com.phenikaa.thesisservice.dto.request.RegisterTopicRequest;
import com.phenikaa.thesisservice.entity.ProjectTopic;
import com.phenikaa.thesisservice.entity.Register;
import com.phenikaa.thesisservice.mapper.RegisterMapper;
import com.phenikaa.thesisservice.repository.ProjectTopicRepository;
import com.phenikaa.thesisservice.repository.RegisterRepository;
import com.phenikaa.thesisservice.service.interfaces.RegisterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class RegisterServiceImpl implements RegisterService {

    private final RegisterMapper registerMapper;
    private final RegisterRepository registerRepository;
    private final ProjectTopicRepository projectTopicRepository;

    @Override
    public Register registerTopic(RegisterTopicRequest dto, Integer userId) {
        Register register = registerMapper.toRegister(dto);

        ProjectTopic topic = projectTopicRepository.findById(dto.getTopicId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Topic not found!"));

        topic.setApprovalStatus(ProjectTopic.ApprovalStatus.PENDING);
        projectTopicRepository.save(topic);

        register.setProjectTopic(topic);

        register.setStudentId(userId);

        return registerRepository.save(register);
    }
}
