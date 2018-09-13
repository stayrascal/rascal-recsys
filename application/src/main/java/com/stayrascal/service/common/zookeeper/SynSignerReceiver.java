package com.stayrascal.service.common.zookeeper;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.function.Consumer;

public class SynSignerReceiver implements Watcher {
    private static final Logger LOGGER = LoggerFactory.getLogger(SynSignerReceiver.class);

    private String path;
    private Consumer<String> consumer;
    private ActiveKeyValueStore store = new ActiveKeyValueStore();

    public SynSignerReceiver(String hosts, String path, Consumer<String> consumer) throws IOException, InterruptedException {
        this.path = path;
        this.consumer = consumer;
        store.connect(hosts);
    }

    public void process() throws KeeperException, InterruptedException {
        String value = store.read(path, this);
        LOGGER.info("Read {} as {}\n", path, value);
        consumer.accept(value);
    }

    @Override
    public void process(WatchedEvent event) {
        if (event.getType() == Event.EventType.NodeDataChanged) {
            try {
                this.process();
            } catch (InterruptedException | KeeperException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException, KeeperException {
        SynSignerReceiver receiver = new SynSignerReceiver("localhost:2181", "/test", System.out::println);
        receiver.process();
        Thread.sleep(Long.MAX_VALUE);
    }
}
