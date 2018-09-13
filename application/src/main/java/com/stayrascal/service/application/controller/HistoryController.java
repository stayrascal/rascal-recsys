package com.stayrascal.service.application.controller;

import com.stayrascal.service.application.constraints.Limits;
import com.stayrascal.service.application.domain.HistoryRecord;
import com.stayrascal.service.application.domain.NumOfUsers;
import com.stayrascal.service.application.domain.TotalFreq;
import com.stayrascal.service.application.dto.result.base.Result;
import com.stayrascal.service.application.dto.result.history.HistoryQueryResult;
import com.stayrascal.service.application.dto.result.history.HistoryQueryResultForUser;
import com.stayrascal.service.application.dto.result.history.NumOfUsersQueryResult;
import com.stayrascal.service.application.dto.result.history.TotalFreqsQueryResult;
import com.stayrascal.service.application.history.HistoryFormatException;
import com.stayrascal.service.application.history.HistoryService;

import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@RestController
@CrossOrigin(origins = "*")
public class HistoryController {

    private HistoryService historyService;

    @Autowired
    public HistoryController(HistoryService historyService) {
        this.historyService = historyService;
    }

    @PostMapping("/api/v1/history")
    public ResponseEntity<Result> addHistoryRecord(@RequestParam("record") String record) {
        historyService.addHistoryRecord(record);
        return ResponseEntity.ok(new HistoryQueryResult(Collections.singletonList(record)));
    }

    @GetMapping("/api/v1/history")
    public ResponseEntity<Result> getHistoryForUser(@RequestParam("userName") String userName,
                                                    @Param("compName") String compName) {
        List<HistoryRecord> historyRecords = historyService.getHistoryForUser(userName, compName);
        return ResponseEntity.ok(new HistoryQueryResultForUser(historyRecords));
    }

    @GetMapping("/api/v1/history/{compName:.+}/quantity")
    public ResponseEntity<Result> getNumOfUsers(@PathVariable("compName") String compName) {
        List<NumOfUsers> numOfUsers = historyService.getNumOfUsers(compName);
        return ResponseEntity.ok(new NumOfUsersQueryResult(numOfUsers));
    }

    @GetMapping("/api/v1/history/total")
    public ResponseEntity<Result> getTotalFreq(@RequestParam("compName") String compName) {
        List<TotalFreq> totalFreqs = historyService.getTotalFreq(compName);
        return ResponseEntity.ok(new TotalFreqsQueryResult(totalFreqs));
    }

    @GetMapping("/api/v1/history/popular/population")
    public ResponseEntity<Result> getPopularUsagesPopulation(@RequestParam("limit") int limit) {
        limit = Math.min(limit, Limits.MAX_HISTORY_NUM);
        List<NumOfUsers> numOfUsers = historyService.getPopularUsagesPopulation(limit);
        return ResponseEntity.ok(new NumOfUsersQueryResult(numOfUsers));
    }

    @GetMapping("/api/v1/history/popular/count")
    public ResponseEntity<Result> getPopulatedUsagesCount(@RequestParam("limit") int limit) {
        limit = Math.min(limit, Limits.MAX_HISTORY_NUM);
        List<TotalFreq> totalFreqs = historyService.getPopulatedUsagesCount(limit);
        return ResponseEntity.ok(new TotalFreqsQueryResult(totalFreqs));
    }

    @ExceptionHandler(HistoryFormatException.class)
    public ResponseEntity handleHistoryFormatError(HistoryFormatException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}
