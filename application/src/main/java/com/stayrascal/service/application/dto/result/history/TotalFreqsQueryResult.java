package com.stayrascal.service.application.dto.result.history;

import com.stayrascal.service.application.domain.TotalFreq;
import com.stayrascal.service.application.dto.result.QueryResult;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class TotalFreqsQueryResult extends QueryResult<TotalFreq> {
    public TotalFreqsQueryResult(int numFound, int start, List<TotalFreq> data) {
        super(numFound, start, data);
    }

    public TotalFreqsQueryResult(int numFound, List<TotalFreq> data) {
        super(numFound, data);
    }

    public TotalFreqsQueryResult(List<TotalFreq> data) {
        super(data);
    }

    @JsonProperty("totalFreqs")
    @Override
    public List<TotalFreq> getData() {
        return super.getData();
    }
}
