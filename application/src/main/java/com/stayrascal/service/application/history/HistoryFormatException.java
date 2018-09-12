package com.stayrascal.service.application.history;

public class HistoryFormatException extends HistoryException {
    public HistoryFormatException(String message) {
        super(message);
    }

    public HistoryFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}
