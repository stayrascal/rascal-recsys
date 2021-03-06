package com.stayrascal.service.application.dto.result.thesaurus;

import com.stayrascal.service.application.dto.result.QueryResult;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class WordQueryResult extends QueryResult<String> {
    public WordQueryResult(int numFound, int start, List<String> data) {
        super(numFound, start, data);
    }

    public WordQueryResult(int numFound, List<String> data) {
        super(numFound, data);
    }

    public WordQueryResult(List<String> data) {
        super(data);
    }

    @JsonProperty("word")
    @Override
    public List<String> getData() {
        return super.getData();
    }
}
