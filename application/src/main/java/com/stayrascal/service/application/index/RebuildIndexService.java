package com.stayrascal.service.application.index;

import com.stayrascal.service.application.component.ComponentService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RebuildIndexService {
    @Autowired
    private ComponentService componentService;

    public void rebuildIndex() {
        try {
            componentService.rebuild();
        } catch (Exception e) {
            throw new RebuildIndexException("Fail to rebuild index", e);
        }
    }
}
