package com.stayrascal.service.application.storage;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class FileSystemStorageService implements StorageService<Path> {
    private final Path rootLocation;
    private final ConcurrentSkipListSet<Path> pathSet = new ConcurrentSkipListSet<>();

    @Autowired
    public FileSystemStorageService(StorageProperties properties) {
        this.rootLocation = Paths.get(properties.getLocation());
    }

    @Override
    public void init(String... args) {
        try {
            for (String subDir : args) {
                Files.createDirectories(rootLocation.resolve(subDir));
            }
        } catch (IOException e) {
            throw new StorageException("Could not initialize storage", e);
        }
    }

    @Async
    @Override
    public CompletableFuture<Optional<Path>> store(MultipartFile file, String dir) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (file.isEmpty()) {
                    throw new StorageException("Failed to store empty file " + file.getOriginalFilename());
                }
                Path target = this.rootLocation.resolve(dir).resolve(file.getOriginalFilename());
                if (!pathSet.contains(target)) {
                    Files.copy(file.getInputStream(), target);
                    pathSet.add(target);
                    return Optional.of(target);
                } else {
                    return Optional.empty();
                }
            } catch (IOException e) {
                throw new StorageException("Failed to store file " + file.getOriginalFilename(), e);
            }
        });
    }

    @Override
    public CompletableFuture<Stream<Path>> store(MultipartFile[] files, String dir) {
        List<CompletableFuture<Optional<Path>>> fileStoreFutures = Stream.of(files)
                .map(file -> store(file, dir))
                .collect(Collectors.toList());
        return CompletableFuture
                .allOf(fileStoreFutures.toArray(new CompletableFuture[files.length]))
                .thenApply(ignore ->
                        fileStoreFutures.stream()
                                .map(CompletableFuture::join)
                                .filter(Optional::isPresent)
                                .map(Optional::get));
    }

    @Override
    public Stream<Path> loadAll(String path) {
        try {
            Path dir = this.rootLocation.resolve(path);
            return Files.walk(dir, 1)
                    .filter(path1 -> !path1.equals(dir))
                    .map(dir::relativize);
        } catch (IOException e) {
            throw new StorageException("Failed to read stored files", e);
        }
    }

    @Override
    public Path load(String subDir, String filename) {
        return rootLocation.resolve(subDir).resolve(filename);
    }

    @Override
    public Resource loadAsResource(String subDir, String filename) {
        try {
            Path file = load(subDir, filename);
            UrlResource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new StorageFileNotFoundException("Could not read file: " + filename);
            }
        } catch (MalformedURLException e) {
            throw new StorageFileNotFoundException("Could not read file: " + filename, e);
        }
    }

    @Override
    public void deleteAll(boolean deleteDir, String... subDirs) {
        for (String subdir : subDirs) {
            try {
                if (deleteDir) {
                    FileSystemUtils.deleteRecursively(rootLocation.resolve(subdir).toFile());
                } else {
                    FileUtils.cleanDirectory(rootLocation.resolve(subdir).toFile());
                }
            } catch (IOException e) {
                throw new StorageException("Could not delete directory: " + subdir, e);
            }
        }
    }

    @Override
    public void deleteAll(Stream<Path> pathStream) {
        for (Path path : pathStream.toArray(Path[]::new)) {
            delete(path);
        }
    }

    @Override
    public void delete(Path path) {
        try {
            Files.delete(path);
        } catch (IOException e) {
            throw new StorageException("Cannot delete file: " + path.getFileName());
        }
    }
}
