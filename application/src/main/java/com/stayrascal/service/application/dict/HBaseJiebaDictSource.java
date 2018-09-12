package com.stayrascal.service.application.dict;


import com.stayrascal.service.analysis.jieba.repository.DictSource;
import com.stayrascal.service.application.repository.JiebaDictRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.function.Consumer;

@Repository
public class HBaseJiebaDictSource implements DictSource {
    @Autowired
    private JiebaDictRepository jiebaDictRepository;

    @Override
    public void loadDict(Charset charset, Consumer<String[]> consumer) throws IOException {
        this.loadDict(consumer);
    }

    @Override
    public void loadDict(Consumer<String[]> consumer) throws IOException {
        jiebaDictRepository.loadAll().stream()
                .map(word -> new String[]{word.getName(), String.valueOf(word.getWeight()), word.getTag()})
                .forEach(consumer);
    }
}
