package com.phenikaa.thesisservice.service.implement;

import com.phenikaa.thesisservice.dto.request.SuggestTopicDTO;
import com.phenikaa.thesisservice.entity.ProjectTopic;
import com.phenikaa.thesisservice.entity.SuggestedTopic;
import com.phenikaa.thesisservice.mapper.SuggestTopicMapper;
import com.phenikaa.thesisservice.repository.ProjectTopicRepository;
import com.phenikaa.thesisservice.repository.SuggestRepository;
import com.phenikaa.thesisservice.service.interfaces.SuggestService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SuggestServiceImpl implements SuggestService {
    private final SuggestTopicMapper suggestTopicMapper;
    private final ProjectTopicRepository projectTopicRepository;
    private final SuggestRepository suggestRepository;

    @Override
    public void suggestTopic(SuggestTopicDTO dto, Integer studentId) {

        ProjectTopic topic = suggestTopicMapper.toProjectTopic(dto);
        topic.setCreatedBy(studentId);
        projectTopicRepository.save(topic);

        SuggestedTopic suggested = SuggestedTopic.builder()
                .topicId(topic.getTopicId())
                .suggestedBy(studentId)
                .suggestedFor(dto.getSupervisorId())
                .reason(dto.getReason())
                .suggestionStatus(SuggestedTopic.SuggestionStatus.PENDING)
                .build();
        suggestRepository.save(suggested);
    }

    @Override
    public void acceptSuggestedTopic(Integer suggestedId, Integer approverId) {
        SuggestedTopic suggestion = suggestRepository.findById(suggestedId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đề xuất"));

        if (suggestion.getSuggestionStatus() != SuggestedTopic.SuggestionStatus.PENDING) {
            throw new RuntimeException("Đề xuất đã được xử lý trước đó");
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
