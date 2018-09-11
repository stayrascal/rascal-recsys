package com.stayrascal.service.application.common;

import java.nio.file.Path;
import java.util.stream.Stream;

public interface FileImporter {
    interface Reader<T> {
        Stream<T> read(Stream<Path> paths);
    }

    interface Writer<T> {
        void write(Stream<T> components);
    }

    void importFromFiles(Stream<Path> paths);
}
