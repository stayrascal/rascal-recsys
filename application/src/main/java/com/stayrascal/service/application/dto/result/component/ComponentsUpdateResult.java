package com.stayrascal.service.application.dto.result.component;

import com.stayrascal.service.application.domain.Component;
import com.stayrascal.service.application.dto.result.UpdateResult;

import java.util.List;

public class ComponentsUpdateResult extends UpdateResult<Component> {
    public ComponentsUpdateResult(List<Component> data) {
        super(data);
    }
}
