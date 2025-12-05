package com.example.productservice.exception;

public class ConflictException extends RuntimeException {

    private final String errorCode;

    public ConflictException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public ConflictException(String message) {
        super(message);
        this.errorCode = "CONFLICT";
    }

    public String getErrorCode() {
        return errorCode;
    }
}
