package com.stayrascal.service.application.common;

import java.nio.file.Path;
import java.util.stream.Stream;

public abstract class AbstractFileImporter<T> implements FileImporter {
    private Reader<T> reader = getReader();
    private Writer<T> writer = getWriter();

    protected abstract Reader<T> getReader();

    protected abstract Writer<T> getWriter();

    public void importFromFiles(Stream<Path> paths) {
        writer.write(reader.read(paths));
    }
}
