package com.stayrascal.service.application.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public interface StorageService<T> {
    void init(String... args);

    CompletableFuture<Optional<T>> store(MultipartFile file, String dir);

    CompletableFuture<Stream<T>> store(MultipartFile[] files, String dir);

    Stream<T> loadAll(String path);

    T load(String subDir, String filename);

    Resource loadAsResource(String subDir, String filename);

    void deleteAll(boolean deleteDir, String... subDirs);

    void deleteAll(Stream<T> pathStream);

    default void deleteAll(String... subDirs) {
        deleteAll(true, subDirs);
    }

    void delete(T path);
}
