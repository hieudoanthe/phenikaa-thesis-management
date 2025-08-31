package com.phenikaa.thesisservice.service.interfaces;

import com.phenikaa.thesisservice.dto.request.SuggestTopicRequest;
import com.phenikaa.thesisservice.dto.response.GetSuggestTopicResponse;
import org.springframework.data.domain.Page;

public interface SuggestService {
    void suggestTopic(SuggestTopicRequest dto, Integer studentId);
    Page<GetSuggestTopicResponse> getSuggestTopicByStudentId(Integer studentId, int page, int size );

}
