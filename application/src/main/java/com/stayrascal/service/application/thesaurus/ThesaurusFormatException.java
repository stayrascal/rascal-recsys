package com.stayrascal.service.application.thesaurus;

public class ThesaurusFormatException extends ThesaurusException {
    public ThesaurusFormatException(String message) {
        super(message);
    }

    public ThesaurusFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}
