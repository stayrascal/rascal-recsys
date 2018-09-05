package com.stayrascal.recom.offline;

import com.stayrascal.recom.common.RedisUtil;

import com.alibaba.fastjson.JSON;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.FastIDSet;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.model.DataModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

public class UserItemsSimilarityTableRedisWriter {
    private final Logger logger = LoggerFactory.getLogger(UserItemsSimilarityTableRedisWriter.class);
    private DataModel dataModel = null;
    private Jedis jedis = null;
    private CountDownLatch latch = new CountDownLatch(1);

    public UserItemsSimilarityTableRedisWriter(DataModel dataModel) {
        this.dataModel = dataModel;
        this.jedis = RedisUtil.getJedis();
    }

    public void storeToRedis() {
        Executors.newSingleThreadExecutor().submit(() -> {
            process();
            latch.countDown();
        });
    }

    public void waitUtilDone() throws InterruptedException {
        latch.await();
    }

    private void process() {
        try {
            LongPrimitiveIterator iterator = dataModel.getItemIDs();
            while (iterator.hasNext()) {
                long userId = iterator.nextLong();
                FastIDSet iDSet = dataModel.getItemIDsFromUser(userId);
                String key = "UI:" + userId;
                String value = JSON.toJSONString(iDSet.toArray());
                jedis.set(key, value);
                logger.info("Stored User:" + key);
            }
        } catch (TasteException e) {
            e.printStackTrace();
        }
    }
}
