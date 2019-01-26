package com.stayrascal.service.application;

import com.stayrascal.service.application.config.SolrProperties;
import com.stayrascal.service.application.constraints.StorageDirs;
import com.stayrascal.service.application.dict.DictStateSynService;
import com.stayrascal.service.application.dict.DictSynProperties;
import com.stayrascal.service.application.event.EventKafkaProperties;
import com.stayrascal.service.application.history.HistoryKafkaProperties;
import com.stayrascal.service.application.history.HistoryService;
import com.stayrascal.service.application.prediction.PredictionService;
import com.stayrascal.service.application.storage.StorageProperties;
import com.stayrascal.service.application.storage.StorageService;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;

@MapperScan("com.stayrascal.service.application.repository")
@EnableAsync
@SpringBootApplication
@EnableConfigurationProperties({StorageProperties.class, SolrProperties.class, HistoryKafkaProperties.class, DictSynProperties.class, EventKafkaProperties.class})
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public CommandLineRunner init(StorageService storageService,
                                  PredictionService predictionService,
                                  HistoryService historyService,
                                  DictStateSynService dictStateSynService,
                                  DictSynProperties dictSynProperties) {
        return args -> {
            storageService.deleteAll(".");
            storageService.init(StorageDirs.names());
            dictStateSynService.init(dictSynProperties.getZkHosts(), dictSynProperties.getZkPath());
            predictionService.init();
            historyService.init();
        };
    }

    @Bean
    public ExitCodeGenerator exitCodeGenerator() {
        return () -> 42;
    }
}
