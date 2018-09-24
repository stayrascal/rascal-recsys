package com.stayrascal.service.application.domain;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Item {
  private String uuid;
  private int id;
  private String title;
  private String content;
  private String describe;
  private String link;
  private Set<String> tags = new HashSet<>();

  public String getUuid() {
    return uuid;
  }

  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public String getDescribe() {
    return describe;
  }

  public void setDescribe(String describe) {
    this.describe = describe;
  }

  public String getLink() {
    return link;
  }

  public void setLink(String link) {
    this.link = link;
  }

  public Set<String> getTags() {
    return tags;
  }

  public String getTagStr() {
    return String.join(",", tags);
  }

  public void setTags(Set<String> tags) {
    this.tags = tags;
  }

  public void addTag(String tag) {
    this.tags.add(tag);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null || getClass() != obj.getClass()) return false;
    Item item = (Item) obj;
    return Objects.equals(id, item.getId()) &&
            Objects.equals(uuid, item.getUuid()) &&
            Objects.equals(title, item.getTitle()) &&
            Objects.equals(content, item.getContent()) &&
            Objects.equals(link, item.getLink()) &&
            Objects.equals(describe, item.getDescribe());
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, title, describe, content, link);
  }

  @Override
  public String toString() {
    return "Item{" +
            "id='" + id +
            "', uuid='" + uuid +
            "', title=" + title +
            "', content='" + content +
            "', describe='" + describe +
            "', link='" + link +
            "', tags=[" + String.join(",", tags) +
            "]}";
  }
}
