package com.stayrascal.recom.offline;

import com.stayrascal.recom.common.ItemSimilarity;
import com.stayrascal.recom.common.RedisUtil;

import com.alibaba.fastjson.JSON;
import org.apache.mahout.cf.taste.similarity.precompute.SimilarItem;
import org.apache.mahout.cf.taste.similarity.precompute.SimilarItems;
import org.apache.mahout.cf.taste.similarity.precompute.SimilarItemsWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import java.io.IOException;

public class ItemSimilarityTableRedisWriter implements SimilarItemsWriter {
    private final Logger logger = LoggerFactory.getLogger(ItemSimilarityTableRedisWriter.class);
    private long itemCounter = 0;
    private Jedis jedis = null;

    @Override
    public void open() throws IOException {
        jedis = RedisUtil.getJedis();
    }

    @Override
    public void add(SimilarItems similarItems) throws IOException {
        ItemSimilarity[] values = new ItemSimilarity[similarItems.numSimilarItems()];
        int counter = 0;
        for (SimilarItem item : similarItems.getSimilarItems()) {
            values[counter] = new ItemSimilarity(item.getItemID(), item.getSimilarity());
            counter++;
        }
        String key = "II:" + similarItems.getItemID();
        String items = JSON.toJSONString(values);
        jedis.set(key, items);
        itemCounter++;
        if (itemCounter % 100 == 0) {
            logger.info("Store {} to redis, total: {}", key, itemCounter);
        }
    }

    @Override
    public void close() throws IOException {
        jedis.close();
    }
}
