package com.stayrascal.service.application.segment;

import com.stayrascal.service.analysis.jieba.JiebaSegmenter;
import com.stayrascal.service.analysis.jieba.Pair;
import com.stayrascal.service.analysis.jieba.WordDictionary;
import com.stayrascal.service.analysis.jieba.repository.DictSource;
import com.stayrascal.service.application.dict.DictLoadException;
import com.stayrascal.service.application.dict.DictStateSynService;
import com.stayrascal.service.application.domain.Word;
import com.stayrascal.service.application.repository.JiebaDictRepository;

import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
public class JiebaSegmentService implements SegmentService, DisposableBean {
    private DictSource dictSource;
    private JiebaSegmenter jiebaSegmenter;
    private JiebaDictRepository dictRepository;
    private PublishSubject<Optional> synSignalPublisher;
    private Disposable disposableForSyn;

    @Autowired
    public JiebaSegmentService(DictSource dictSource, JiebaDictRepository jiebaDictRepository, DictStateSynService synService) {
        this.dictSource = dictSource;
        this.dictRepository = jiebaDictRepository;
        this.synSignalPublisher = PublishSubject.create();
        disposableForSyn = this.synSignalPublisher
                .debounce(5, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .subscribe(signal -> synService.requestSync());
        init();
    }

    private void init() {
        System.setProperty("jieba.defaultDict", "false");
        try {
            WordDictionary.getInstance().loadUserDict(dictSource);
        } catch (IOException e) {
            throw new DictLoadException("Fail to load user dict", e);
        }
        jiebaSegmenter = new JiebaSegmenter();
        jiebaSegmenter.subscribe(pairs -> {
            updateDictDB(pairs);
            synSignalPublisher.onNext(Optional.empty());
        });
    }

    @Override
    public void updateDictDB(List<Pair<String>> changes) {
        changes.parallelStream()
                .forEach(stringPair -> dictRepository.updateWordWithNoTag(stringPair.key, stringPair.freq.longValue()));
    }

    @Override
    public List<String> sentenceProcess(String sentence, boolean HMM) {
        return jiebaSegmenter.sentenceProcess(sentence, HMM);
    }

    @Override
    public Optional<Word> searchWord(String name) {
        Word word = dictRepository.getWordByName(name);
        if (word != null) {
            return Optional.of(word);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Pair<String> suggestFreq(String... words) {
        return suggestFreq(false, words);
    }

    @Override
    public Pair<String> suggestFreq(String word) {
        return suggestFreq(false, word);
    }

    @Override
    public Pair<String> tuneFreq(String... words) {
        return suggestFreq(true, words);
    }

    @Override
    public Pair<String> tuneFreq(String word) {
        return suggestFreq(true, word);
    }

    private Pair<String> suggestFreq(boolean tune, String... words) {
        long freq = jiebaSegmenter.suggestFreq(tune, words);
        return new Pair<>(String.join("", words), freq);
    }

    private Pair<String> suggestFreq(boolean tune, String word) {
        long freq = jiebaSegmenter.suggestFreq(tune, word);
        return new Pair<>(word, freq);
    }

    @Override
    public void destroy() {
        disposableForSyn.dispose();
    }
}
