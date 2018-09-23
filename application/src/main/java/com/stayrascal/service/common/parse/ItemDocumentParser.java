package com.stayrascal.service.common.parse;

import com.stayrascal.service.application.domain.Item;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ItemDocumentParser {
  private List<Item> items;
  private Pattern pattern = Pattern.compile("(\n?<[^>]*>)|(\\([^)]*\\))");

  public boolean parse(Document doc) {
    Element content_el = doc.getElementById("content");
    Element method_summary_el = content_el.getElementsByTag("table").get(1);
    Elements elements = method_summary_el.getElementsByTag("tbody").select("tr");
    elements.parallelStream()
            .map(element -> {
              Item item = new Item();
              item.setContent(element.select(".content").text());
              item.setTitle(element.select(".title").text());
              item.setLink(element.select(".link").text());
              item.setUuid(UUID.randomUUID().toString());
              item.setDescribe(element.select(".description").text());
              return item;
            }).collect(Collectors.toList());
    return true;
  }

  public List<Item> getItems() {
    return items;
  }
}
