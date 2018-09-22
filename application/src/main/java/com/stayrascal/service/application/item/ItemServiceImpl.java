package com.stayrascal.service.application.item;

import com.stayrascal.service.application.common.AbstractFileImporter;
import com.stayrascal.service.application.constraints.EnvVariables;
import com.stayrascal.service.application.domain.Item;
import com.stayrascal.service.application.repository.ItemRepository;
import com.stayrascal.service.common.parse.CompDocumentParser;

import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

@Service
public class ItemServiceImpl extends AbstractFileImporter<Item> implements ItemService {
  private ItemRepository repository;

  @Autowired
  public ItemServiceImpl(ItemRepository repository) {
    this.repository = repository;
  }

  @Override
  public Optional<Item> searchItems(String uuid) {
    try {
      Item item = repository.getItemByUUID(uuid);
      return item == null ? Optional.empty() : Optional.of(item);
    } catch (DataAccessException e) {
      throw new ItemException("Fail to load item: " + uuid, e);
    }


  }

  @Override
  public List<Item> searchItemLike(String titleLike) {
    return null;
  }

  @Override
  public Optional<Item> searchItem(int id) {
    return Optional.empty();
  }

  @Override
  public List<Item> searchItemsByDesc(String desc, int rows) {
    return null;
  }

  @Override
  public List<Item> searchItemsByTitle(String title, int rows) {
    return null;
  }

  @Override
  public List<Item> searchItemsByContent(String content, int rows) {
    return null;
  }

  @Override
  public List<Item> searchItemsByTag(String tag, int rows) {
    return null;
  }

  @Override
  public Item updateItem(Item item) {
    repository.updateItem(item);
    return item;
  }

  @Override
  public void deleteItem(String id) {
    repository.deleteItemById(id);
  }

  @Override
  public Item addItem(Item item) {
    repository.addItem(item);
    return item;
  }

  @Override
  @Transactional
  @Async
  public CompletableFuture<Void> rebuild() {
    int offset = 0;
    int limit = 150;
    while (true) {
      List<Item> page = repository.getPagedItems(offset, limit);
      if (page.size() > 0) {
        page.parallelStream().forEach(repository::updateItem);
        offset += limit;
      } else {
        break;
      }
    }
    return CompletableFuture.completedFuture(null);
  }

  @Override
  protected Reader<Item> getReader() {
    return paths -> {
      CompDocumentParser parser = new CompDocumentParser();
      return paths.parallel().flatMap(path -> {
        try {
          if (parser.parse(Jsoup.parse(path.toFile(), EnvVariables.DEFAULT_CHARSET))) {
            return parser.getRecords().stream().map(record -> new Item());
          }
        } catch (IOException e) {
          e.printStackTrace();
        }
        return Stream.empty();
      });
    };
  }

  @Override
  protected Writer<Item> getWriter() {
    return items -> items.forEach(repository::addItem);
  }

  @Override
  public void importFromFiles(Stream<Path> paths) {
    super.importFromFiles(paths);
  }
}
