package com.phenikaa.submissionservice.exception;

public class SubmissionStatusException extends RuntimeException {
    
    public SubmissionStatusException(String message) {
        super(message);
    }
    
    public SubmissionStatusException(String message, Throwable cause) {
        super(message, cause);
    }
}
