package com.stayrascal.service.application.component;

public class ComponentNotFoundException extends ComponentException {

    public ComponentNotFoundException(String message) {
        super(message);
    }

    public ComponentNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
