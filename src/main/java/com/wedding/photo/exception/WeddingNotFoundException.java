package com.wedding.photo.exception;

public class WeddingNotFoundException extends RuntimeException {
    public WeddingNotFoundException(String message) {
        super(message);
    }
}