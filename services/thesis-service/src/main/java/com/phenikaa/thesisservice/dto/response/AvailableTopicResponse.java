package com.phenikaa.thesisservice.dto.response;

import com.phenikaa.thesisservice.entity.ProjectTopic;
import lombok.Data;

@Data
public class AvailableTopicResponse {
    private Integer topicId;
    private String topicCode;
    private String title;
    private String description;
    private Integer maxStudents;
    private Integer currentStudents;
    private Integer supervisorId;
    private ProjectTopic.DifficultyLevel difficultyLevel;

    // Custom Builder Pattern
    public static AvailableTopicResponseBuilder builder() {
        return new AvailableTopicResponseBuilder();
    }

    public static class AvailableTopicResponseBuilder {
        private AvailableTopicResponse response;

        public AvailableTopicResponseBuilder() {
            response = new AvailableTopicResponse();
        }

        public AvailableTopicResponseBuilder topicId(Integer topicId) {
            response.topicId = topicId;
            return this;
        }

        public AvailableTopicResponseBuilder topicCode(String topicCode) {
            response.topicCode = topicCode;
            return this;
        }

        public AvailableTopicResponseBuilder title(String title) {
            response.title = title;
            return this;
        }

        public AvailableTopicResponseBuilder description(String description) {
            response.description = description;
            return this;
        }

        public AvailableTopicResponseBuilder maxStudents(Integer maxStudents) {
            response.maxStudents = maxStudents;
            return this;
        }

        public AvailableTopicResponseBuilder currentStudents(Integer currentStudents) {
            response.currentStudents = currentStudents;
            return this;
        }

        public AvailableTopicResponseBuilder supervisorId(Integer supervisorId) {
            response.supervisorId = supervisorId;
            return this;
        }

        public AvailableTopicResponseBuilder difficultyLevel(ProjectTopic.DifficultyLevel difficultyLevel) {
            response.difficultyLevel = difficultyLevel;
            return this;
        }

        public AvailableTopicResponse build() {
            return response;
        }
    }
}
