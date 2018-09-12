package com.stayrascal.service.application.repository;

import com.stayrascal.service.application.domain.Word;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JiebaDictRepository {
    List<Word> loadAll();

    Word getWordByName(@Param("name") String name);

    void updateWordWithNoTag(@Param("name") String name, @Param("weight") long weight);

    void updateWord(@Param("name") String name, @Param("weight") long weight, @Param("tag") String tag);
}
