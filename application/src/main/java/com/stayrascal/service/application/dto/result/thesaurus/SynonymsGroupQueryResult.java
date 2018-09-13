package com.stayrascal.service.application.dto.result.thesaurus;

import com.stayrascal.service.application.domain.SynonymsGroup;
import com.stayrascal.service.application.dto.result.QueryResult;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class SynonymsGroupQueryResult extends QueryResult<SynonymsGroup> {
    public SynonymsGroupQueryResult(int numFound, List<SynonymsGroup> data) {
        super(numFound, data);
    }

    public SynonymsGroupQueryResult(List<SynonymsGroup> data) {
        super(data);
    }

    @JsonProperty("synonymsGroup")
    @Override
    public List<SynonymsGroup> getData() {
        return super.getData();
    }
}
