package com.stayrascal.service.application.dto.result.base;

public abstract class AbstractResult<T> implements Result<T> {
    private T data;

    public AbstractResult(T data) {
        this.data = data;
    }

    @Override
    public T getData() {
        return data;
    }
}
