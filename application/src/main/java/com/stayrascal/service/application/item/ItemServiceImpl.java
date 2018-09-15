package com.stayrascal.service.application.item;

import com.stayrascal.service.application.domain.Item;

import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

@Service
public class ItemServiceImpl implements ItemService {
    @Override
    public Optional<Item> searchItems(String name) {
        return Optional.empty();
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
        return null;
    }

    @Override
    public void deleteItem(String id) {

    }

    @Override
    public Item addItem(Item component) {
        return null;
    }

    @Override
    public CompletableFuture<Void> rebuild() {
        return null;
    }

    @Override
    public void importFromFiles(Stream<Path> paths) {

    }
}
