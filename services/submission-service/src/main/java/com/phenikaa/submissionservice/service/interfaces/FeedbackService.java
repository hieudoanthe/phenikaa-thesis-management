package com.phenikaa.submissionservice.service.interfaces;

import com.phenikaa.submissionservice.dto.request.FeedbackRequest;
import com.phenikaa.submissionservice.dto.response.FeedbackResponse;
import com.phenikaa.submissionservice.entity.Feedback;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface FeedbackService {
    FeedbackResponse createFeedback(FeedbackRequest request);
    FeedbackResponse updateFeedback(Integer feedbackId, FeedbackRequest request);
    FeedbackResponse getFeedbackById(Integer feedbackId);
    List<FeedbackResponse> getFeedbacksBySubmission(Integer submissionId);
    List<FeedbackResponse> getFeedbacksByReviewer(Integer reviewerId);
    Page<FeedbackResponse> getFeedbacksWithPagination(Pageable pageable);
    void deleteFeedback(Integer feedbackId);
    FeedbackResponse approveFeedback(Integer feedbackId);
    Double calculateAverageScore(Integer submissionId);
    FeedbackResponse convertToResponse(Feedback feedback);

}
