package com.stayrascal.service.application.thesaurus;

import com.stayrascal.service.application.common.FileImporter;

import java.nio.file.Path;
import java.util.stream.Stream;

public interface ThesaurusImporter extends FileImporter {

    void importThesaurus(Stream<Path> pathStream);
}
