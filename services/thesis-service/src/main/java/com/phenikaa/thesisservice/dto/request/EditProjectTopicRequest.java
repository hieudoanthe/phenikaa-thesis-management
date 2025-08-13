package com.phenikaa.thesisservice.dto.request;

import com.phenikaa.thesisservice.entity.ProjectTopic;
import lombok.Data;

@Data
public class EditProjectTopicRequest {
    private Integer topicId;
    private String topicCode;
    private String title;
    private Integer academicYearId;
    private Integer maxStudents;
    private ProjectTopic.ApprovalStatus approvalStatus;
    private ProjectTopic.TopicStatus topicStatus;
}
