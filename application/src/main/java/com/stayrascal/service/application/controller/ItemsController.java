package com.stayrascal.service.application.controller;

import static com.stayrascal.service.application.constraints.Limits.MAX_ITEMS_QUERY_NUM;

import com.stayrascal.service.application.dto.result.base.Result;
import com.stayrascal.service.application.item.ItemService;
import com.stayrascal.service.application.storage.StorageService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Path;

@RestController
public class ItemsController {

    private ItemService service;
    private StorageService<Path> storageService;
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    public ItemsController(ItemService itemService, StorageService<Path> storageService) {
        this.service = itemService;
        this.storageService = storageService;
    }

    @GetMapping(value = "/api/v1/items")
    public ResponseEntity<Result> searchItems(@RequestParam("desc") String desc,
                                              @RequestParam("title") String title,
                                              @RequestParam("content") String content,
                                              @RequestParam(value = "row", defaultValue = "10") int rows) {
        rows = Math.min(rows, MAX_ITEMS_QUERY_NUM);
        service.searchItems(title);
        return null;
    }
}
