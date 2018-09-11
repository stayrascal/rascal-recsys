package com.stayrascal.service.application.thesaurus;

public class ThesaurusImportException extends ThesaurusException {
    public ThesaurusImportException(String message) {
        super(message);
    }

    public ThesaurusImportException(String message, Throwable cause) {
        super(message, cause);
    }
}
