package com.stayrascal.recom.service;

import com.stayrascal.recom.common.ItemSimilarity;
import com.stayrascal.recom.common.RedisUtil;

import com.alibaba.fastjson.JSON;
import redis.clients.jedis.Jedis;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/ws/v1/recom")
public class ItemBasedRecommendResult {
    private Jedis jedis = null;

    public ItemBasedRecommendResult() {
        this.jedis = RedisUtil.getJedis();
    }

    @GET
    @Path("/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public RecommendedItems getRecomItems(@PathParam("userId") String userId) {
        RecommendedItems recommendedItems = new RecommendedItems();

        TreeSet<Long> userItemsSet = getUserItems(userId);
        if (userItemsSet == null) {
            return recommendedItems;
        }

        recommendedItems.setItems(getSimilarItems(userItemsSet));
        return recommendedItems;
    }

    private Long[] getSimilarItems(TreeSet<Long> userItemsSet) {
        List<String> similarItems = jedis.mget(userItemsSet
                .stream()
                .map(item -> "II:" + item)
                .toArray(String[]::new));

        Set<ItemSimilarity> similarItemSet = similarItems.stream()
                .flatMap(item -> JSON.parseArray(item, ItemSimilarity.class).stream())
                .collect(Collectors.toSet());

        return similarItemSet.stream()
                .filter(item -> !userItemsSet.contains(item.getItemId()))
                .map(ItemSimilarity::getItemId)
                .limit(10)
                .toArray(Long[]::new);
    }

    private TreeSet<Long> getUserItems(String userId) {
        String userItemsStr = jedis.get(String.format("UI:%s", userId));
        if (userItemsStr == null || userItemsStr.length() <= 0) {
            return null;
        }
        return new TreeSet<>(JSON.parseArray(userItemsStr, Long.class));
    }
}
