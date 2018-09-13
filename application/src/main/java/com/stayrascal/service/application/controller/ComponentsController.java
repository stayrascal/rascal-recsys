package com.stayrascal.service.application.controller;

import static com.stayrascal.service.application.constraints.Limits.MAX_COMPS_QUERY_NUM;

import com.stayrascal.service.application.component.ComponentAlreadyExistsException;
import com.stayrascal.service.application.component.ComponentNotFoundException;
import com.stayrascal.service.application.component.ComponentService;
import com.stayrascal.service.application.constraints.Error;
import com.stayrascal.service.application.constraints.StorageDirs;
import com.stayrascal.service.application.domain.Component;
import com.stayrascal.service.application.dto.result.ErrorResult;
import com.stayrascal.service.application.dto.result.base.Result;
import com.stayrascal.service.application.dto.result.component.ComponentsQueryResult;
import com.stayrascal.service.application.dto.result.component.ComponentsUpdateResult;
import com.stayrascal.service.application.dto.result.storage.FilesQueryResult;
import com.stayrascal.service.application.storage.StorageService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "*")
public class ComponentsController {

    private ComponentService service;
    private StorageService<Path> storageService;
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    public ComponentsController(ComponentService service, StorageService<Path> storageService) {
        this.service = service;
        this.storageService = storageService;
    }

    @GetMapping(value = "/api/v1/comps")
    public ResponseEntity<Result> searchComps(@RequestParam("desc") String desc,
                                              @RequestParam(value = "rows", defaultValue = "10") int rows) {
        logger.debug("Try to search components with filter param: desc={}, rows={}", desc, rows);
        rows = Math.min(rows, MAX_COMPS_QUERY_NUM);
        logger.debug("Searching components");
        List<Component> components = service.searchComps(desc, rows);
        ComponentsQueryResult queryResult = new ComponentsQueryResult(components);
        logger.debug("There are {} components found.", components.size());
        return ResponseEntity.ok(queryResult);
    }

    @GetMapping(value = "/api/v1/comps/{name:.+}")
    public ResponseEntity<Result> searchComp(@PathVariable("name") String name) {
        logger.debug("Try to find specified component with id: {}", name);
        List<Component> components = service.searchCompLike(name);
        return ResponseEntity.ok(new ComponentsQueryResult(components));
    }

    @PostMapping(value = "/api/v1/comps")
    public ResponseEntity<Result> addComp(@RequestBody Component component) {
        logger.debug("Adding component: {}", component.getId());
        ComponentsQueryResult queryResult = new ComponentsQueryResult(
                Collections.singletonList(service.addComp(component)));
        logger.debug("Component: {} added.", component.getId());
        return ResponseEntity.ok(queryResult);
    }

    @DeleteMapping(value = "/api/v1/comps/{id:.+}")
    public ResponseEntity deleteComp(@PathVariable("id") String id) {
        logger.debug("Deleting component: {}", id);
        service.deleteComp(id);
        logger.debug("Component: {} has deleted.", id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping(value = "/api/v1/comps")
    public ResponseEntity<Result> updateComp(@RequestBody Component component) {
        logger.debug("Updating component: {}", component.getId());
        ComponentsUpdateResult result = new ComponentsUpdateResult(
                Collections.singletonList(service.updateComp(component)));
        logger.debug("Update component: {} has done.", component.getId());
        return ResponseEntity.ok(result);
    }

    @PostMapping(value = "/api/v1/comps/upload")
    public CompletableFuture<ResponseEntity<Result>> importComps(@RequestParam("files") MultipartFile[] files) {
        logger.debug(Arrays.toString(files));
        return storageService.store(files, StorageDirs.COMPS.name())
                .thenApply(pathStream -> {
                    List<Path> paths = pathStream.collect(Collectors.toList());
                    service.importFromFiles(paths.stream());
                    return paths;
                })
                .thenApply(paths -> {
                    storageService.deleteAll(paths.stream());
                    return paths;
                })
                .handle((paths, throwable) -> {
                    if (throwable != null) {
                        Error error = new Error(throwable.getMessage(), 104);
                        storageService.deleteAll(paths.stream());
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResult(error));
                    } else {
                        return ResponseEntity.ok(new FilesQueryResult(paths.stream()
                                .map(path -> path.getFileName().toString())
                                .collect(Collectors.toList())));
                    }
                });
    }

    @ExceptionHandler(ComponentNotFoundException.class)
    public ResponseEntity handleComponentNotFound(ComponentNotFoundException e) {
        Error error = new Error(e.getMessage(), 101);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResult(error));
    }

    @ExceptionHandler(ComponentAlreadyExistsException.class)
    public ResponseEntity handleComponentAlreadyExists(ComponentAlreadyExistsException e) {
        Error error = new Error(e.getMessage(), 102);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResult(error));
    }
}
