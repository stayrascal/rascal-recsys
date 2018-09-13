package com.stayrascal.service.application.dto.result;

import com.stayrascal.service.application.constraints.Error;
import com.stayrascal.service.application.dto.result.base.Result;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.LinkedList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ErrorResult implements Result<List<Error>> {
    private List<Error> errors;

    public ErrorResult() {
        this.errors = new LinkedList<>();
    }

    public ErrorResult(Error error) {
        this.errors = new LinkedList<>();
        this.errors.add(error);
    }

    public ErrorResult(List<Error> errors) {
        this.errors = errors;
    }

    public void addError(Error error) {
        this.errors.add(error);
    }

    @Override
    @JsonProperty("errors")
    public List<Error> getData() {
        return errors;
    }
}
