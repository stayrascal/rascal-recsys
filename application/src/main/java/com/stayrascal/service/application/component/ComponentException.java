package com.stayrascal.service.application.component;

public class ComponentException extends RuntimeException {
    public ComponentException(String message) {
        super(message);
    }

    public ComponentException(String message, Throwable cause) {
        super(message, cause);
    }
}
