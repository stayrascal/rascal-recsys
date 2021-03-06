package com.stayrascal.service.application.dto.result.storage;

import com.stayrascal.service.application.dto.result.QueryResult;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public final class FilesQueryResult extends QueryResult<String> {

    public FilesQueryResult(int numFound, int start, List<String> data) {
        super(numFound, start, data);
    }

    public FilesQueryResult(int numFound, List<String> data) {
        super(numFound, data);
    }

    public FilesQueryResult(List<String> data) {
        super(data);
    }

    @JsonProperty("files")
    @Override
    public List<String> getData() {
        return super.getData();
    }
}
