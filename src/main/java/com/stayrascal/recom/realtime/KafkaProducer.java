package com.stayrascal.recom.realtime;

import com.stayrascal.recom.common.Constants;

import com.alibaba.fastjson.JSON;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class KafkaProducer implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaProducer.class);
    private final String topic;

    public KafkaProducer(String topic) {
        this.topic = topic;
    }

    static NewClickEvent[] newClickEvents = new NewClickEvent[]{
            new NewClickEvent(1000000L, 123L),
            new NewClickEvent(1000001L, 111L),
            new NewClickEvent(1000002L, 500L),
            new NewClickEvent(1000003L, 278L),
            new NewClickEvent(1000004L, 681L),
    };

    @Override
    public void run() {
        Properties props = new Properties();
//        props.put("metadata.broker.list", Constants.KAFKA_ADDR);
//        props.put("key.serializer", "kafka.serializer.StringEncoder");
//        props.put("producer.type", "async");
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, Constants.KAFKA_ADDR);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        Producer<Integer, String> producer = null;
        try {
            LOGGER.info("Producing messages");
            producer = new org.apache.kafka.clients.producer.KafkaProducer<>(props);
            for (NewClickEvent event : newClickEvents) {
                String eventAsStr = JSON.toJSONString(event);
                producer.send(new ProducerRecord<Integer, String>(this.topic, eventAsStr));
                LOGGER.info("Sending message: {}", eventAsStr);
            }
            LOGGER.info("Done sending messages");
        } catch (Exception ex) {
            LOGGER.error("Error while producing messages", ex);
            LOGGER.trace(null, ex);
            System.err.println("Error while producing messages: " + ex);
        } finally {
            if (producer != null) {
                producer.close();
            }
        }
    }

    public static void main(String[] args) {
        new Thread(new KafkaProducer(Constants.KAFKA_TOPICS)).start();
    }
}
