package com.stayrascal.recom.offline;

import org.apache.mahout.cf.taste.impl.recommender.GenericItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.LogLikelihoodSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.precompute.MultithreadedBatchItemSimilarities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SimilarityTablesGenerator {
    private static final Logger logger = LoggerFactory.getLogger(SimilarityTablesGenerator.class);

    private SimilarityTablesGenerator() {
    }

    public static void main(String[] args) throws Exception {
        GroupLensDataModel dataModel = new GroupLensDataModel();
        UserItemsSimilarityTableRedisWriter writer = new UserItemsSimilarityTableRedisWriter(dataModel);
        writer.storeToRedis();

        GenericItemBasedRecommender recommender = new GenericItemBasedRecommender(dataModel, new LogLikelihoodSimilarity(dataModel));
        MultithreadedBatchItemSimilarities batch = new MultithreadedBatchItemSimilarities(recommender, 5);
        int numSimilarities = batch.computeItemSimilarities(Runtime.getRuntime().availableProcessors(), 1, new ItemSimilarityTableRedisWriter());
        logger.info("Computed {} similarities to {} items and saved them to redis", numSimilarities, dataModel.getNumItems());
        writer.waitUtilDone();
    }
}
