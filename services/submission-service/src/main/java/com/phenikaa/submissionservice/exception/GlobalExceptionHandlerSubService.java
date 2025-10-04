package com.phenikaa.submissionservice.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandlerSubService {

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
    public ResponseEntity<Map<String, Object>> handleMaxUploadSizeExceeded(MaxUploadSizeExceededException ex) {
        log.error("File upload size exceeded: {}", ex.getMessage());
        
        Map<String, Object> errorResponse = Map.of(
            "error", "PAYLOAD_TOO_LARGE",
            "message", "File quá lớn. Vui lòng chọn file nhỏ hơn 50MB.",
            "timestamp", LocalDateTime.now().toString(),
            "status", HttpStatus.PAYLOAD_TOO_LARGE.value()
        );
        
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(MethodArgumentNotValidException ex) {
        log.error("Validation error: {}", ex.getMessage());
        
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> 
            fieldErrors.put(error.getField(), error.getDefaultMessage()));
        
        Map<String, Object> errorResponse = Map.of(
            "error", "VALIDATION_ERROR",
            "message", "Dữ liệu không hợp lệ",
            "fieldErrors", fieldErrors,
            "timestamp", LocalDateTime.now().toString(),
            "status", HttpStatus.BAD_REQUEST.value()
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    @ExceptionHandler(SubmissionStatusException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<Map<String, Object>> handleSubmissionStatusException(SubmissionStatusException ex) {
        log.error("Submission status error: {}", ex.getMessage());
        
        Map<String, Object> errorResponse = Map.of(
            "error", "SUBMISSION_STATUS_ERROR",
            "message", ex.getMessage(),
            "timestamp", LocalDateTime.now().toString(),
            "status", HttpStatus.INTERNAL_SERVER_ERROR.value()
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
    
    @ExceptionHandler(ReportSubmissionException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<Map<String, Object>> handleReportSubmissionException(ReportSubmissionException ex) {
        log.error("Report submission error: {}", ex.getMessage());
        
        Map<String, Object> errorResponse = Map.of(
            "error", "REPORT_SUBMISSION_ERROR",
            "message", ex.getMessage(),
            "timestamp", LocalDateTime.now().toString(),
            "status", HttpStatus.INTERNAL_SERVER_ERROR.value()
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
    
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<Map<String, Object>> handleGeneralException(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        
        Map<String, Object> errorResponse = Map.of(
            "error", "INTERNAL_SERVER_ERROR",
            "message", "Có lỗi xảy ra. Vui lòng thử lại sau.",
            "timestamp", LocalDateTime.now().toString(),
            "status", HttpStatus.INTERNAL_SERVER_ERROR.value()
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}
