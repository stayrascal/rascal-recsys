package com.stayrascal.service.analysis.jieba.repository;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

public class InputStreamDictSource implements DictSource {
    private InputStream is;

    public InputStreamDictSource(InputStream inputStream) {
        this.is = inputStream;
    }

    @Override
    public void loadDict(Charset charset, Consumer<String[]> consumer) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, charset))) {
            while (reader.ready()) {
                String line = reader.readLine();
                String[] tokens = line.split("[\t ]+");
                consumer.accept(tokens);
            }
        }
    }

    @Override
    public void loadDict(Consumer<String[]> consumer) throws IOException {
        this.loadDict(StandardCharsets.UTF_8, consumer);
    }
}
