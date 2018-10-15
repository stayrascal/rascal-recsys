package com.stayrascal.service.application.index;

import com.stayrascal.service.application.component.ComponentService;
import com.stayrascal.service.application.item.ItemService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RebuildIndexService {
    @Autowired
    private ComponentService componentService;

    @Autowired
    private ItemService itemService;

    public void rebuildIndex() {
        try {
            itemService.rebuild();
            componentService.rebuild();
        } catch (Exception e) {
            throw new RebuildIndexException("Fail to rebuild index", e);
        }
    }
}
