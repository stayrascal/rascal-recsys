package com.stayrascal.service.application.thesaurus;

public class SynonymsGroupNotExistException extends ThesaurusException {
    public SynonymsGroupNotExistException(String message) {
        super(message);
    }

    public SynonymsGroupNotExistException(String message, Throwable cause) {
        super(message, cause);
    }
}
