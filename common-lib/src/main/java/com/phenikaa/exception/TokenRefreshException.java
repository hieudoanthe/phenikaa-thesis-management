package com.phenikaa.exception;


import java.io.Serial;

public class TokenRefreshException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    public TokenRefreshException(String message) {
        super(message);
    }

    public TokenRefreshException(String message, Throwable cause) {
        super(message, cause);
    }
}
