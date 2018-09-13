package com.stayrascal.service.application.controller;

import static com.stayrascal.service.application.thesaurus.SynonymsConvertor.parseToSet;

import com.stayrascal.service.analysis.jieba.Pair;
import com.stayrascal.service.application.constraints.Error;
import com.stayrascal.service.application.constraints.StorageDirs;
import com.stayrascal.service.application.dict.DictLoadException;
import com.stayrascal.service.application.domain.SynonymsGroup;
import com.stayrascal.service.application.domain.Word;
import com.stayrascal.service.application.dto.result.ErrorResult;
import com.stayrascal.service.application.dto.result.base.Result;
import com.stayrascal.service.application.dto.result.dict.DictQueryResult;
import com.stayrascal.service.application.dto.result.segment.SegmentResult;
import com.stayrascal.service.application.dto.result.segment.TuneResult;
import com.stayrascal.service.application.dto.result.storage.FilesQueryResult;
import com.stayrascal.service.application.dto.result.thesaurus.CombineResult;
import com.stayrascal.service.application.dto.result.thesaurus.SynonymsGroupQueryResult;
import com.stayrascal.service.application.segment.SegmentException;
import com.stayrascal.service.application.segment.SegmentService;
import com.stayrascal.service.application.storage.StorageService;
import com.stayrascal.service.application.thesaurus.ThesaurusException;
import com.stayrascal.service.application.thesaurus.ThesaurusFormatException;
import com.stayrascal.service.application.thesaurus.ThesaurusImportException;
import com.stayrascal.service.application.thesaurus.ThesaurusService;

import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "*")
public class DictionariesController {
    private SegmentService segmentService;
    private StorageService<Path> storageService;
    private ThesaurusService thesaurusService;

    @Autowired
    public DictionariesController(SegmentService segmentService, StorageService<Path> storageService, ThesaurusService thesaurusService) {
        this.segmentService = segmentService;
        this.storageService = storageService;
        this.thesaurusService = thesaurusService;
    }

    /**
     * 分词服务
     *
     * @param sentence 语句
     * @param action   动作：共3种
     *                 1.  "cut": 分词
     *                 2.  "tune"  调整字典
     *                 3.   "suggest"   只建议，不调整字典
     * @return 响应
     */
    @PostMapping("api/v1/segment")
    public ResponseEntity<Result> tuneDict(@Param("sentence") String sentence,
                                           @Param("action") String action) {
        if (sentence.matches("\\s+")) {
            throw new SegmentException("sentence is empty!");
        }

        String[] parts = sentence.split("\\s+");
        if (parts.length > 2) {
            throw new SegmentException("Num of words should less than 2!");
        }

        Pair<String> suggest;
        switch (action.trim()) {
            case "tune":
                if (parts.length == 1) {
                    suggest = segmentService.tuneFreq(sentence);
                } else {
                    suggest = segmentService.tuneFreq(parts);
                }
                break;
            case "suggest":
                if (parts.length == 1) {
                    suggest = segmentService.suggestFreq(sentence);
                } else {
                    suggest = segmentService.suggestFreq(parts);
                }
                break;
            case "cut":
                List<String> words = segmentService.sentenceProcess(sentence, false);
                return ResponseEntity.ok(new SegmentResult(words));
            default:
                throw new SegmentException("Unsupported action: " + action);
        }
        return ResponseEntity.ok(new TuneResult(suggest));
    }

    /**
     * 获得分词库中具体某个单词的信息
     *
     * @param word 单词
     * @return 响应
     */
    @GetMapping("/api/v1/segment/{word}")
    public ResponseEntity<Result> getWord(@PathVariable("word") String word) {
        Optional<Word> quantum = segmentService.searchWord(word);
        return quantum.<ResponseEntity<Result>>map(w -> ResponseEntity.ok(new DictQueryResult(Collections.singletonList(w))))
                .orElseGet(() -> ResponseEntity.ok(new DictQueryResult(Collections.emptyList())));
    }

    /**
     * 获得指定单词的同义词组
     *
     * @param word 单词
     * @return 响应
     */
    @GetMapping("/api/v1/thesaurus/synonyms/{word}")
    public ResponseEntity<Result> getSynonymGroups(@PathVariable("word") String word) {
        List<SynonymsGroup> synonymsGroup = thesaurusService.searchSynonyms(word);
        return ResponseEntity.ok(new SynonymsGroupQueryResult(synonymsGroup));
    }

    /**
     * 从同义词组中删除一个单词或删除整个同义词组
     *
     * @param word    单词，若不指定则删除整个同义词组
     * @param groupId 词组ID
     * @return 响应
     */
    @DeleteMapping("/api/v1/thesaurus/synonyms")
    public ResponseEntity deleteWordFromSynonymsGroup(@RequestParam(value = "word", required = false) String word,
                                                      @RequestParam("groupId") Integer groupId) {
        if (word == null) {
            thesaurusService.deleteSynonymsGroup(groupId);
        } else {
            thesaurusService.deleteWordFromSynonymsGroup(word, groupId);
        }
        return ResponseEntity.noContent().build();
    }

    /**
     * 添加一组词到已有的同义词组中
     *
     * @param words   词组
     * @param groupId 词组ID，若不指定则新建一组同义词组
     * @return 响应
     */
    @PostMapping("/api/v1/thesaurus/synonyms")
    public ResponseEntity<Result> addWordsToGroup(@RequestParam("words") String words,
                                                  @RequestParam(value = "groupId", required = false) Integer groupId) {
        Set<String> syns = parseToSet(words);
        if (syns.size() <= 1) {
            throw new ThesaurusFormatException("Synonyms must be contains at least 2 words.");
        }
        SynonymsGroup synonymsGroup = new SynonymsGroup(syns);
        if (groupId == null) {
            thesaurusService.addSynonymGroup(synonymsGroup);
        } else {
            thesaurusService.addWordsToSynonymsGroup(syns, groupId);
            synonymsGroup.setGroupId(groupId);
        }
        return ResponseEntity.ok(new SynonymsGroupQueryResult(Collections.singletonList(synonymsGroup)));
    }

    /**
     * 组合多组同义词为一组
     *
     * @param map 同义词ID
     * @return 响应
     */
    @PostMapping("/api/v1/thesaurus/synonyms/combine")
    public ResponseEntity<Result> combineSynonymsGroup(@RequestBody Map<String, Integer[]> map) {
        Integer[] groupIds = map.get("groupIds");
        if (groupIds == null) {
            throw new ThesaurusException("Counld not found params: groupIds ");
        }
        SynonymsGroup combination = thesaurusService.combineSynonymsGroups(groupIds);
        return ResponseEntity.ok(new CombineResult(Collections.singletonList(combination)));
    }


    /**
     * 上传同义字典文件，文件格式参照“/resource/comp_synonyms.txt
     *
     * @param files 同义字典文件
     * @return 响应
     */
    @PostMapping("/api/v1/thesaurus/upload")
    public CompletableFuture<ResponseEntity<Result>> importThesaurus(@RequestParam("files") MultipartFile[] files) {
        return storageService.store(files, StorageDirs.THESAURUS.name())
                .thenApply(pathStream -> {
                    List<Path> paths = pathStream.collect(Collectors.toList());
                    thesaurusService.importFromFiles(paths.stream());
                    return paths;
                }).thenApply(paths -> {
                    storageService.deleteAll(paths.stream());
                    return paths;
                }).handle((paths, throwable) -> {
                    if (throwable != null) {
                        Error error = new Error(throwable.getMessage(), 204);
                        try {
                            storageService.deleteAll(paths.stream());
                        } catch (NullPointerException e) {
//                            e.printStackTrace();
                        }
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(new ErrorResult(error));
                    } else {
                        return ResponseEntity.ok(new FilesQueryResult(paths.stream()
                                .map(path -> path.getFileName().toString())
                                .collect(Collectors.toList())));
                    }
                });
    }

    @ExceptionHandler(SegmentException.class)
    public ResponseEntity handleSegmentException(SegmentException e) {
        Error error = new Error(e.getMessage(), 201);
        return ResponseEntity.badRequest().body(new ErrorResult(error));
    }

    @ExceptionHandler(DictLoadException.class)
    public ResponseEntity handleDictLoadException(DictLoadException e) {
        Error error = new Error(e.getMessage(), 202);
        return ResponseEntity.badRequest().body(new ErrorResult(error));
    }

    @ExceptionHandler(ThesaurusFormatException.class)
    public ResponseEntity handleThesaurusStateException(ThesaurusFormatException e) {
        Error error = new Error(e.getMessage(), 203);
        return ResponseEntity.badRequest().body(new ErrorResult(error));
    }

    @ExceptionHandler(ThesaurusImportException.class)
    public ResponseEntity handleThesaurusImportException(ThesaurusImportException e) {
        Error error = new Error(e.getMessage(), 204);
        return ResponseEntity.badRequest().body(new ErrorResult(error));
    }


}
