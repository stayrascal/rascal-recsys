package com.stayrascal.service.analysis.jieba.repository;

import java.nio.charset.Charset;
import java.util.List;
import java.util.function.Consumer;

public class PureDictSource implements DictSource {

    private List<String> records;

    public PureDictSource(List<String> records) {
        this.records = records;
    }

    @Override
    public void loadDict(Charset charset, Consumer<String[]> consumer) {
        records.forEach(record -> consumer.accept(record.split("[\t ]+")));
    }

    @Override
    public void loadDict(Consumer<String[]> consumer) {
        records.forEach(record -> consumer.accept(record.split("[\t ]+")));
    }
}
