package com.stayrascal.service.application.dto.result.recommend;

import com.stayrascal.service.application.domain.Prediction;
import com.stayrascal.service.application.dto.result.QueryResult;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class PredictionResult extends QueryResult<Prediction> {


    public PredictionResult(int numFound, int start, List<Prediction> data) {
        super(numFound, start, data);
    }

    public PredictionResult(int numFound, List<Prediction> data) {
        super(numFound, data);
    }

    public PredictionResult(List<Prediction> data) {
        super(data);
    }

    @JsonProperty("prediction")
    @Override
    public List<Prediction> getData() {
        return super.getData();
    }
}
