package com.phenikaa.submissionservice.exception;

public class ReportSubmissionException extends RuntimeException {
    
    public ReportSubmissionException(String message) {
        super(message);
    }
    
    public ReportSubmissionException(String message, Throwable cause) {
        super(message, cause);
    }
}
