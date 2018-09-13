package com.stayrascal.service.application.dto.result.history;

import com.stayrascal.service.application.domain.NumOfUsers;
import com.stayrascal.service.application.dto.result.QueryResult;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class NumOfUsersQueryResult extends QueryResult<NumOfUsers> {
    public NumOfUsersQueryResult(int numFound, int start, List<NumOfUsers> data) {
        super(numFound, start, data);
    }

    public NumOfUsersQueryResult(int numFound, List<NumOfUsers> data) {
        super(numFound, data);
    }

    public NumOfUsersQueryResult(List<NumOfUsers> data) {
        super(data);
    }

    @JsonProperty("numOfUsers")
    @Override
    public List<NumOfUsers> getData() {
        return super.getData();
    }
}
