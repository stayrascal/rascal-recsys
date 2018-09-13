package com.stayrascal.service.application.dto.result.component;

import com.stayrascal.service.application.domain.Component;
import com.stayrascal.service.application.dto.result.QueryResult;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class ComponentsQueryResult extends QueryResult<Component> {

    public ComponentsQueryResult(int numFound, int start, List<Component> data) {
        super(numFound, start, data);
    }

    public ComponentsQueryResult(int numFound, List<Component> data) {
        super(numFound, data);
    }

    public ComponentsQueryResult(List<Component> data) {
        super(data);
    }

    @JsonProperty("comps")
    @Override
    public List<Component> getData() {
        return super.getData();
    }
}
