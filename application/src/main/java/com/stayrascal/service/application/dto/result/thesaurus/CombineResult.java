package com.stayrascal.service.application.dto.result.thesaurus;

import com.stayrascal.service.application.domain.SynonymsGroup;
import com.stayrascal.service.application.dto.result.QueryResult;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

class CombineResult extends QueryResult<SynonymsGroup> {
    public CombineResult(int numFound, int start, List<SynonymsGroup> data) {
        super(numFound, start, data);
    }

    public CombineResult(int numFound, List<SynonymsGroup> data) {
        super(numFound, data);
    }

    public CombineResult(List<SynonymsGroup> data) {
        super(data);
    }

    @JsonProperty("combination")
    @Override
    public List<SynonymsGroup> getData() {
        return super.getData();
    }
}
