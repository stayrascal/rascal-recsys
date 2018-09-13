package com.stayrascal.service.common.zookeeper;

import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class SynSignerSender {
    private static final Logger LOGGER = LoggerFactory.getLogger(SynSignerSender.class);
    private String path;
    private ActiveKeyValueStore store = new ActiveKeyValueStore();

    public SynSignerSender(String hosts, String path) throws IOException, InterruptedException {
        this.path = path;
        store.connect(hosts);
    }

    public void sendSynSignal(String value) throws KeeperException, InterruptedException {
        store.write(path, value);
        LOGGER.info("Set {} to {}\n", path, value);
    }

    public String getPath() {
        return path;
    }

    public static void main(String[] args) throws IOException, InterruptedException, KeeperException {
        SynSignerSender sender = new SynSignerSender("localhost:2181", "/test");
        Random random = new Random();
        while (true) {
            String value = random.nextInt(100) + "";
            sender.sendSynSignal(value);
            TimeUnit.SECONDS.sleep(random.nextInt(10));
        }
    }
}
