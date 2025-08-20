package com.phenikaa.thesisservice.service.implement;

import com.phenikaa.thesisservice.client.NotificationServiceClient;
import com.phenikaa.dto.request.NotificationRequest;
import com.phenikaa.thesisservice.dto.request.SuggestTopicRequest;
import com.phenikaa.thesisservice.entity.ProjectTopic;
import com.phenikaa.thesisservice.entity.SuggestedTopic;
import com.phenikaa.thesisservice.mapper.SuggestTopicMapper;
import com.phenikaa.thesisservice.repository.ProjectTopicRepository;
import com.phenikaa.thesisservice.repository.SuggestRepository;
import com.phenikaa.thesisservice.service.interfaces.SuggestService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SuggestServiceImpl implements SuggestService {
    private final SuggestTopicMapper suggestTopicMapper;
    private final ProjectTopicRepository projectTopicRepository;
    private final SuggestRepository suggestRepository;
    private final NotificationServiceClient notificationServiceClient;


    @Override
    public void suggestTopic(SuggestTopicRequest dto, Integer studentId) {

        ProjectTopic topic = suggestTopicMapper.toProjectTopic(dto);
        topic.setCreatedBy(studentId);
        topic.setTopicCode(UUID.randomUUID().toString());
        topic.setTopicStatus(ProjectTopic.TopicStatus.ACTIVE);
        projectTopicRepository.save(topic);

        SuggestedTopic suggested = SuggestedTopic.builder()
                .projectTopic(topic)
                .suggestedBy(studentId)
                .suggestedFor(dto.getSupervisorId())
                .reason(dto.getReason())
                .suggestionStatus(SuggestedTopic.SuggestionStatus.PENDING)
                .build();
        suggestRepository.save(suggested);

        NotificationRequest noti = new NotificationRequest(
                studentId,
                dto.getSupervisorId(),
                "Bạn có một đề tài mới cần duyệt!"
        );
        notificationServiceClient.sendNotification(noti);
    }

    @Override
    public void acceptSuggestedTopic(Integer suggestedId, Integer approverId) {
        SuggestedTopic suggestion = suggestRepository.findById(suggestedId)
                .orElseThrow(() -> new RuntimeException("Not found suggested topic with id: " + suggestedId + " or it has been deleted by supervisor"));

        if (suggestion.getSuggestionStatus() != SuggestedTopic.SuggestionStatus.PENDING) {
            throw new RuntimeException("The proposal has been processed before!");
        }

        suggestion.setSuggestionStatus(SuggestedTopic.SuggestionStatus.APPROVED);
        suggestion.setApprovedBy(approverId);

        ProjectTopic topic = suggestion.getProjectTopic();
        topic.setApprovalStatus(ProjectTopic.ApprovalStatus.AVAILABLE);
        topic.setTopicStatus(ProjectTopic.TopicStatus.ACTIVE);

        projectTopicRepository.save(topic);
        suggestRepository.save(suggestion);
    }


}
