package com.phenikaa.evalservice.exception;

public class DefenseSessionNotFoundException extends RuntimeException {
    
    public DefenseSessionNotFoundException(String message) {
        super(message);
    }
    
    public DefenseSessionNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
