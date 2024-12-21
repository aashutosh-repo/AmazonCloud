package com.test.microservices.error;

public class NotificationException extends RuntimeException {
    public NotificationException(String message) {
        super(message);
    }
}