package com.stayrascal.service.application.component;

public class ComponentLoadException extends ComponentException {
    public ComponentLoadException(String message) {
        super(message);
    }

    public ComponentLoadException(String message, Throwable cause) {
        super(message, cause);
    }
}
