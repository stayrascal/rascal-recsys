package com.stayrascal.service.application.item;

import com.stayrascal.service.application.common.FileImporter;
import com.stayrascal.service.application.domain.Item;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface ItemService extends FileImporter {
  Optional<Item> searchItems(String uuid);

  List<Item> searchItemLike(String titleLike);

  Optional<Item> searchItem(Long id);

  List<Item> searchItemsByDesc(String desc, int rows);

  List<Item> searchItemsByTitle(String title, int rows);

  List<Item> searchItemsByTitleOrDesc(String query, int rows);

  List<Item> searchItemsByContent(String content, int rows);

  List<Item> searchItemsByTag(String tag, int rows);

  Item updateItem(Item item);

  void deleteItemByID(Long id);

  void deleteItemByUUID(String UUID);

  Item addItem(Item component);

  CompletableFuture<Void> rebuild();
}
