package com.stayrascal.service.application.event;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "kafka.event")
public class EventKafkaProperties implements Serializable {
  private List<String> topics;

  private Map<String, Object> kafkaParamsConsumer;


  private Map<String, Object> kafkaParamsProducer;

  private String checkpointDir = System.getProperty("java.io.tmpdir");


  public List<String> getTopics() {
    return topics;
  }

  public void setTopics(List<String> topics) {
    this.topics = topics;
  }

  public String getCheckpointDir() {
    return checkpointDir;
  }

  public void setCheckpointDir(String checkpointDir) {
    this.checkpointDir = checkpointDir;
  }

  public Map<String, Object> getKafkaParamsConsumer() {
    return kafkaParamsConsumer;
  }

  public void setKafkaParamsConsumer(Map<String, Object> kafkaParamsConsumer) {
    this.kafkaParamsConsumer = kafkaParamsConsumer;
  }

  public Map<String, Object> getKafkaParamsProducer() {
    return kafkaParamsProducer;
  }

  public void setKafkaParamsProducer(Map<String, Object> kafkaParamsProducer) {
    this.kafkaParamsProducer = kafkaParamsProducer;
  }
}
