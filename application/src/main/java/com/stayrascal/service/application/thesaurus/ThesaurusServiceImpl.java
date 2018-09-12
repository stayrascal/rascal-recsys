package com.stayrascal.service.application.thesaurus;

import static com.stayrascal.service.application.thesaurus.SynonymsConvertor.parseToSet;
import static com.stayrascal.service.application.thesaurus.SynonymsConvertor.parseToString;

import com.stayrascal.service.application.common.AbstractFileImporter;
import com.stayrascal.service.application.domain.SynonymsGroup;
import com.stayrascal.service.application.domain.SynonymsGroupStr;
import com.stayrascal.service.application.repository.ThesaurusRepository;
import com.stayrascal.service.common.parse.SynonymsRecordParser;

import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;
import org.spark_project.jetty.util.ConcurrentHashSet;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ThesaurusServiceImpl extends AbstractFileImporter<SynonymsGroup> implements ThesaurusService, DisposableBean {
    private ThesaurusRepository repository;
    private PublishSubject<Optional<Void>> dbChangeNotifier;
    private Disposable disposable;
    private Set<Integer> IDs = new ConcurrentHashSet<>();

    @Autowired
    public ThesaurusServiceImpl(ThesaurusRepository repository, ThesaurusChangeSensor changeSensor) {
        this.repository = repository;
        this.dbChangeNotifier = PublishSubject.create();
        disposable = this.dbChangeNotifier.subscribe(changeSensor);
    }

    @Override
    protected Reader<SynonymsGroup> getReader() {
        return paths -> paths
                .flatMap(path -> {
                    try {
                        return Files.readAllLines(path).stream();
                    } catch (IOException e) {
                        throw new ThesaurusImportException("Fail to read file: " + path.getFileName(), e);
                    }
                })
                .parallel()
                .map(s -> {
                    SynonymsRecordParser parser = new SynonymsRecordParser();
                    parser.parser(s);
                    return new SynonymsGroup(new HashSet<>(parser.getSynonyms()));
                });
    }

    @Override
    protected Writer<SynonymsGroup> getWriter() {
        return synonymGroups -> {
            synonymGroups.forEach(synonymsGroup -> addSynonymGroup(synonymsGroup, false, true));
            System.out.println("Initial data loaded, begin to solve conflict!");
            this.rebuildThesaurus();
        };
    }

    @Override
    public List<SynonymsGroup> searchSynonyms(String word) {
        return repository.getSynonymsByWord(word)
                .parallelStream()
                .map(SynonymsGroupStr::toSynonymsGroup)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public void deleteWordFromSynonymsGroup(String word, int groupId) {
        SynonymsGroupStr synonymsGroupStr = repository.getSynonymsByGroupId(groupId);
        if (synonymsGroupStr == null) return;
        Set<String> synonyms = parseToSet(synonymsGroupStr.getSynonyms());
        synonyms.remove(word);
        repository.deleteBelong(word, groupId);
        repository.updateSynonymsGroup(groupId, parseToString(synonyms));
        dbChangeNotifier.onNext(Optional.empty());
    }

    @Override
    public void deleteSynonymsGroup(int groupId) {
        repository.deleteSynonymsGroup(groupId);
        repository.deleteBelongGroup(groupId);
        dbChangeNotifier.onNext(Optional.empty());
    }

    @Override
    public SynonymsGroup getSynonymsGroupById(int groupId) {
        return repository.getSynonymsByGroupId(groupId).toSynonymsGroup();
    }

    @Transactional
    @Override
    public void addWordsToSynonymsGroup(Set<String> words, int groupId) {
        SynonymsGroupStr synonymsGroupStr = repository.getSynonymsByGroupId(groupId);
        if (synonymsGroupStr == null) {
            throw new SynonymsGroupNotExistException("The synonyms group for groupId: " + groupId + "is not exists.");
        }
        Set<String> synonyms = parseToSet(synonymsGroupStr.getSynonyms());
        synonyms.addAll(words);
        repository.updateSynonymsGroup(groupId, parseToString(synonyms));
        words.forEach(word -> repository.addBelong(word, groupId));
        dbChangeNotifier.onNext(Optional.empty());
    }

    @Override
    public void addSynonymGroup(SynonymsGroup synonymsGroup) {
        addSynonymGroup(synonymsGroup, true, true);
    }

    @Transactional
    void addSynonymGroup(SynonymsGroup synonymsGroup, boolean notify, boolean addBelong) {
        SynonymsGroupStr synonymsGroupStr = synonymsGroup.toSynonymsGroupStr();
        repository.addSynonymsGroup(synonymsGroupStr);

        Integer groupId = synonymsGroupStr.getGroupId();
        synonymsGroup.setGroupId(groupId);

        if (addBelong) {
            synonymsGroup.getSynonyms().forEach(word -> repository.addBelong(word, groupId));
        }

        if (notify) dbChangeNotifier.onNext(Optional.empty());
    }

    /**
     * 组合多组同义词，并删除原来的同义词组
     *
     * @param groupIDs 同义词组ID
     */
    @Transactional
    public SynonymsGroup combineSynonymsGroups(Integer... groupIDs) {
        Set<Integer> groupSets = new HashSet<>(Arrays.asList(groupIDs));
        Set<String> synonyms = combineSynonymsGroups(groupSets);
        groupSets.parallelStream().forEach(this::deleteSynonymsGroup);
        SynonymsGroup synonymsGroup = new SynonymsGroup(synonyms);
        addSynonymGroup(synonymsGroup);
        return synonymsGroup;
    }

    /**
     * 将指定的同义词组组合成为一个同义词组
     *
     * @param groupIDs 同义词组ID
     * @return 同义词
     */
    private Set<String> combineSynonymsGroups(Set<Integer> groupIDs) {
        return groupIDs.parallelStream().map(groupId -> repository.getSynonymsByGroupId(groupId))
                .map(SynonymsGroupStr::toSynonymsGroup)
                .map(SynonymsGroup::getSynonyms)
                .reduce((a, b) -> {
                    a.addAll(b);
                    return a;
                }).orElseGet(HashSet::new);
    }

    @Override
    public void addSynonymGroup(String synonyms) {
        Set<String> syns = parseToSet(synonyms);
        if (syns.size() <= 1) {
            throw new ThesaurusFormatException("Synonyms must be contains at least 2 words.");
        }
        this.addSynonymGroup(new SynonymsGroup(syns));
    }

    @Override
    public void importFromFiles(Stream<Path> paths) {
        super.importFromFiles(paths);
        dbChangeNotifier.onNext(Optional.empty());
    }

    @Override
    public void destroy() throws Exception {
        disposable.dispose();
    }

    private void rebuildBelongTo() {
        int offset = 0;
        int limit = 150;
        while (true) {
            List<SynonymsGroupStr> page = repository.getPagedSynonymsGroups(offset, limit);
            if (page.size() > 0) {
                page.parallelStream().map(SynonymsGroupStr::toSynonymsGroup)
                        .forEach(synonymsGroup ->
                                synonymsGroup.getSynonyms().parallelStream()
                                        .forEach(word -> repository.addBelong(word, synonymsGroup.getGroupId())));
                offset += limit;
            } else {
                break;
            }
        }
    }

    /**
     * 重建同义词字典，并处理冲突
     */
    private void rebuildThesaurus() {
        try {
            Path thesaurusPath = solveConflict();

            repository.rebuildSynonymsGroupTable();
            Files.readAllLines(thesaurusPath).parallelStream()
                    .map(SynonymsGroupStr::new)
                    .map(SynonymsGroupStr::toSynonymsGroup)
                    .forEach(synonymsGroup -> this.addSynonymGroup(synonymsGroup, false, false));
            repository.rebuildSynonymsBelongTable();
            this.rebuildBelongTo();

            IDs.clear();
            Files.deleteIfExists(thesaurusPath);
        } catch (IOException e) {
            throw new ThesaurusImportException("Fail to import thesaurus", e);
        }
    }

    private Path solveConflict() throws IOException {
        int offset = 0;
        int limit = 0;
        Path thesaurusPath = Files.createTempFile("thesaurus", "dict");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(thesaurusPath.toFile(), true))) {
            while (true) {
                List<SynonymsGroup> page = repository.getPagedSynonymsGroups(offset, limit)
                        .parallelStream()
                        .map(SynonymsGroupStr::toSynonymsGroup)
                        .collect(Collectors.toList());
                if (page.size() > 0) {
                    for (SynonymsGroup synonymsGroup : page) {
                        Set<String> synonyms = combineSynonymsGroups(combineGroupIDs(synonymsGroup));
                        if (synonyms.size() > 0) {
                            writer.append(parseToString(synonyms)).append("\n");
                        }
                    }
                    writer.flush();
                    offset += limit;
                } else {
                    break;
                }
            }
        }
        return thesaurusPath;
    }

    /**
     * 找到与指定同义词组同义的所有同义词组的ID
     *
     * @param synonymsGroup 同义词组
     * @return 同义词组ID
     */
    private Set<Integer> combineGroupIDs(SynonymsGroup synonymsGroup) {
        if (IDs.contains(synonymsGroup.getGroupId())) {
            return new HashSet<>();
        }

        IDs.add(synonymsGroup.getGroupId());
        HashSet<Integer> groupIds = synonymsGroup.getSynonyms().parallelStream()
                .map(word -> repository.getGroupIdByWord(word))
                .map(HashSet::new)
                .reduce((a, b) -> {
                    a.addAll(b);
                    return a;
                }).orElseGet(HashSet::new);
        Set<Integer> difference = groupIds
                .parallelStream()
                .filter(groupId -> !IDs.contains(groupId))
                .collect(Collectors.toSet());
        groupIds.addAll(difference.parallelStream()
                .map(repository::getSynonymsByGroupId)
                .map(SynonymsGroupStr::toSynonymsGroup)
                .map(this::combineGroupIDs)
                .reduce((a, b) -> {
                    a.addAll(b);
                    return a;
                }).orElseGet(HashSet::new));
        return groupIds;
    }
}
