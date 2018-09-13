package com.stayrascal.service.application.dto.result;

import com.stayrascal.service.application.dto.result.base.WithQTimeResult;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class QueryResult<T> extends WithQTimeResult<List<T>> {

    private Integer numFound;
    private Integer start;

    public QueryResult(int numFound, int start, List<T> data) {
        super(data);
        this.numFound = numFound;
        this.start = start;
    }

    public QueryResult(Integer numFound, List<T> data) {
        super(data);
        this.numFound = numFound;
    }

    public QueryResult(List<T> data) {
        super(data);
        this.numFound = data.size();
    }
}
