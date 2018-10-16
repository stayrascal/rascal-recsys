package com.stayrascal.service.application.item;

import com.stayrascal.service.application.common.AbstractFileImporter;
import com.stayrascal.service.application.config.SolrProperties;
import com.stayrascal.service.application.constraints.EnvVariables;
import com.stayrascal.service.application.domain.Item;
import com.stayrascal.service.application.repository.ItemRepository;
import com.stayrascal.service.common.parse.ItemDocumentParser;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ItemServiceImpl extends AbstractFileImporter<Item> implements ItemService {
  private Logger logger = LoggerFactory.getLogger(this.getClass());
  private final SolrClient solrClient;
  private final SolrProperties properties;
  private ItemRepository repository;

  @Autowired
  public ItemServiceImpl(ItemRepository repository, SolrClient solrClient, SolrProperties solrProperties) {
    this.repository = repository;
    this.solrClient = solrClient;
    this.properties = solrProperties;
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
    return searchItemsByTitle(titleLike, 10);
  }

  @Override
  public Optional<Item> searchItem(Long id) {
    try {
      Item item = repository.getItemById(id);
      return item == null ? Optional.empty() : Optional.of(item);
    } catch (DataAccessException e) {
      throw new ItemException("Fail to load item: " + id, e);
    }
  }

  @Override
  public List<Item> searchItemsByDesc(String desc, int rows) {
    SolrQuery query = new SolrQuery("describe:" + desc);
    query.addField("uuid");
    query.addField("describe");
    query.setRows(rows);
    return queryItemsFromSolr(query);
  }

  @Override
  public List<Item> searchItemsByTitle(String title, int rows) {
    SolrQuery query = new SolrQuery("title:" + title);
    query.addField("title");
    query.addField("describe");
    query.setRows(rows);
    return queryItemsFromSolr(query);
  }

  private List<Item> queryItemsFromSolr(SolrQuery query) {
    QueryResponse response;
    try {
      response = solrClient.query(properties.getCollectionName(), query);
    } catch (SolrServerException | IOException e) {
      throw new ItemException("Fail to load item.", e);
    }
    SolrDocumentList documents = response.getResults();
    return documents.stream()
        .map(doc -> {
          String rowKey = (String) doc.get("uuid");
          return repository.getItemByUUID(rowKey);
        })
        .collect(Collectors.toList());
  }

  @Override
  public List<Item> searchItemsByContent(String content, int rows) {
    SolrQuery query = new SolrQuery("content:" + content);
    query.addField("content");
    query.addField("describe");
    query.setRows(rows);
    return queryItemsFromSolr(query);
  }

  @Override
  public List<Item> searchItemsByTag(String tag, int rows) {
    SolrQuery query = new SolrQuery("tag:" + tag);
    query.addField("tag");
    query.addField("describe");
    query.setRows(rows);
    return queryItemsFromSolr(query);
  }

  @Override
  public Item updateItem(Item item) {
    repository.updateItem(item);
    return item;
  }

  @Override
  public void deleteItemByID(Long id) {
    repository.deleteItemById(id);
  }

  @Override
  public void deleteItemByUUID(String UUID) {
    repository.deleteItemByUUID(UUID);
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
      logger.info("Get {} items.", page.size());
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
      ItemDocumentParser parser = new ItemDocumentParser();
      return paths.parallel().flatMap(path -> {
        try {
          if (parser.parse(Jsoup.parse(path.toFile(), EnvVariables.DEFAULT_CHARSET))) {
            return parser.getItems().stream();
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
