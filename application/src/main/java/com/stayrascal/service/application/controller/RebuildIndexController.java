package com.stayrascal.service.application.controller;

import com.stayrascal.service.application.index.RebuildIndexService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "*")
public class RebuildIndexController {

    private RebuildIndexService service;

    public RebuildIndexController(RebuildIndexService service) {
        this.service = service;
    }

    @GetMapping("/api/v1/reindex")
    public ResponseEntity rebuildIndex() {
        service.rebuildIndex();
        return ResponseEntity.ok("success");
    }
}
