package com.phenikaa.evalservice.exception;

public class DefenseSessionValidationException extends RuntimeException {
    
    public DefenseSessionValidationException(String message) {
        super(message);
    }
    
    public DefenseSessionValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
