package com.stayrascal.service.application.dto.result.thesaurus;

import com.stayrascal.service.application.dto.result.QueryResult;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class SynonymsQueryResult extends QueryResult<String> {
    public SynonymsQueryResult(int numFound, int start, List<String> data) {
        super(numFound, start, data);
    }

    public SynonymsQueryResult(int numFound, List<String> data) {
        super(numFound, data);
    }

    public SynonymsQueryResult(List<String> data) {
        super(data);
    }

    @JsonProperty("synonyms")
    @Override
    public List<String> getData() {
        return super.getData();
    }
}
