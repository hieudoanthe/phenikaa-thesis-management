package com.phenikaa.thesisservice.dto.response;

import com.phenikaa.thesisservice.entity.ProjectTopic;
import lombok.Data;

@Data
public class GetThesisResponse {
    private Integer topicId;
    private Integer registerId;
    private Integer suggestedBy;
    private String topicCode;
    private String title;
    private String description;
    private String objectives;
    private String methodology;
    private String expectedOutcome;
    private Integer academicYearId;
    private Integer maxStudents;
    private ProjectTopic.DifficultyLevel difficultyLevel;
    private ProjectTopic.TopicStatus topicStatus;
    private ProjectTopic.ApprovalStatus approvalStatus;

    // Custom Builder Pattern
    public static GetThesisResponseBuilder builder() {
        return new GetThesisResponseBuilder();
    }

    public static class GetThesisResponseBuilder {
        private GetThesisResponse response;

        public GetThesisResponseBuilder() {
            response = new GetThesisResponse();
        }

        public GetThesisResponseBuilder topicId(Integer topicId) {
            response.topicId = topicId;
            return this;
        }

        public GetThesisResponseBuilder registerId(Integer registerId) {
            response.registerId = registerId;
            return this;
        }

        public GetThesisResponseBuilder suggestedBy(Integer suggestedBy) {
            response.suggestedBy = suggestedBy;
            return this;
        }

        public GetThesisResponseBuilder topicCode(String topicCode) {
            response.topicCode = topicCode;
            return this;
        }

        public GetThesisResponseBuilder title(String title) {
            response.title = title;
            return this;
        }

        public GetThesisResponseBuilder description(String description) {
            response.description = description;
            return this;
        }

        public GetThesisResponseBuilder objectives(String objectives) {
            response.objectives = objectives;
            return this;
        }

        public GetThesisResponseBuilder methodology(String methodology) {
            response.methodology = methodology;
            return this;
        }

        public GetThesisResponseBuilder expectedOutcome(String expectedOutcome) {
            response.expectedOutcome = expectedOutcome;
            return this;
        }

        public GetThesisResponseBuilder academicYearId(Integer academicYearId) {
            response.academicYearId = academicYearId;
            return this;
        }

        public GetThesisResponseBuilder maxStudents(Integer maxStudents) {
            response.maxStudents = maxStudents;
            return this;
        }

        public GetThesisResponseBuilder difficultyLevel(ProjectTopic.DifficultyLevel difficultyLevel) {
            response.difficultyLevel = difficultyLevel;
            return this;
        }

        public GetThesisResponseBuilder topicStatus(ProjectTopic.TopicStatus topicStatus) {
            response.topicStatus = topicStatus;
            return this;
        }

        public GetThesisResponseBuilder approvalStatus(ProjectTopic.ApprovalStatus approvalStatus) {
            response.approvalStatus = approvalStatus;
            return this;
        }

        public GetThesisResponse build() {
            return response;
        }
    }
}
