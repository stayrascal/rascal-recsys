package com.stayrascal.service.application.thesaurus;

public class ThesaurusException extends RuntimeException {

    public ThesaurusException(String message) {
        super(message);
    }

    public ThesaurusException(String message, Throwable cause) {
        super(message, cause);
    }
}
