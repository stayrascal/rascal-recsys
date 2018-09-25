package com.stayrascal.service.application.controller;

import static com.stayrascal.service.application.constraints.Limits.MAX_ITEMS_QUERY_NUM;

import com.stayrascal.service.application.constraints.Error;
import com.stayrascal.service.application.constraints.StorageDirs;
import com.stayrascal.service.application.domain.Item;
import com.stayrascal.service.application.dto.result.ErrorResult;
import com.stayrascal.service.application.dto.result.base.Result;
import com.stayrascal.service.application.dto.result.item.ItemsQueryResult;
import com.stayrascal.service.application.dto.result.item.ItemsUpdateResult;
import com.stayrascal.service.application.dto.result.storage.FilesQueryResult;
import com.stayrascal.service.application.item.ItemAlreadyExistException;
import com.stayrascal.service.application.item.ItemNotFoundException;
import com.stayrascal.service.application.item.ItemService;
import com.stayrascal.service.application.storage.StorageService;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    logger.debug("Searching items");
    List<Item> items = service.searchItemsByDesc(desc, rows);
    ItemsQueryResult result = new ItemsQueryResult(items);
    logger.debug("There are {} items found.", items.size());
    return ResponseEntity.ok(result);
  }

  @PostMapping(value = "/api/v1/items")
  public ResponseEntity<Result> addItem(@RequestBody Item item) {
    logger.debug("Adding item: {}", item.getId());
    ItemsQueryResult result = new ItemsQueryResult(Collections.singletonList(service.addItem(item)));
    logger.debug("Item: {} added.", item.getId());
    return ResponseEntity.ok(result);
  }

  @DeleteMapping(value = "/api/v1/items/{id:.+}")
  public ResponseEntity deleteItem(@PathVariable("id") Long id) {
    logger.debug("Deleting item: {}", id);
    service.deleteItemByID(id);
    logger.debug("Item: {} has deleted.", id);
    return ResponseEntity.noContent().build();
  }

  @PatchMapping(value = "/api/v1/items")
  public ResponseEntity<Result> updateItem(@RequestBody Item item) {
    logger.debug("Updating item: {}", item.getId());
    ItemsUpdateResult result = new ItemsUpdateResult(Collections.singletonList(service.updateItem(item)));
    logger.debug("Updating item: {} has done.", item.getId());
    return ResponseEntity.ok(result);
  }

  @PostMapping(value = "/api/v1/items/upload")
  public CompletableFuture<ResponseEntity<Result>> importItems(@RequestParam("files") MultipartFile[] files) {
    logger.debug(Arrays.toString(files));
    return storageService.store(files, StorageDirs.ITEMS.name())
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

  @ExceptionHandler(ItemNotFoundException.class)
  public ResponseEntity handleItemNotFound(ItemNotFoundException e) {
    Error error = new Error(e.getMessage(), 101);
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResult(error));
  }

  @ExceptionHandler(ItemAlreadyExistException.class)
  public ResponseEntity handleItemAlreadyExist(ItemAlreadyExistException e) {
    Error error = new Error(e.getMessage(), 102);
    return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResult(error));
  }
}
