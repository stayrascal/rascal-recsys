package com.stayrascal.service.application.dto.result.segment;

import com.stayrascal.service.analysis.jieba.Pair;
import com.stayrascal.service.application.dto.result.base.Result;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TuneResult implements Result<Pair<String>> {

    private Pair<String> result;

    public TuneResult(Pair<String> result) {
        this.result = result;
    }

    @JsonProperty("suggest")
    @Override
    public Pair<String> getData() {
        return result;
    }
}
