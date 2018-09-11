package com.stayrascal.service.application.thesaurus;

import com.stayrascal.service.application.dict.DictStateSynService;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Component
public class ThesaurusChangeSensor implements Consumer<Optional<Void>>, DisposableBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(ThesaurusChangeSensor.class);
    private DictStateSynService synService;
    private PublishSubject<Optional> synSignalPublisher;
    private Disposable disposableForSyn;

    @Autowired
    public ThesaurusChangeSensor(DictStateSynService stateSynService) {
        this.synService = stateSynService;
        this.synSignalPublisher = PublishSubject.create();
        disposableForSyn = synSignalPublisher
                .debounce(10, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .subscribe(signal -> synService.requestSync());
    }

    @Override
    public void accept(Optional<Void> aVoid) {
        synSignalPublisher.onNext(Optional.empty());
    }

    @Override
    public void destroy() throws Exception {
        disposableForSyn.dispose();
    }
}
