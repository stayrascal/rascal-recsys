package com.stayrascal.recom.cf.common

object SimilarityMeasures {
  def coocurrence(numOfRatersForAAndB: Long, numOfRatesForA: Long, numOfRatersForB: Long): Double = {
    numOfRatersForAAndB / math.sqrt(numOfRatesForA * numOfRatersForB)
  }

  /**
    * The correlation between two vectors A, B is cov(A, B) / (stdDev(A) * stdDev(B))
    * This is equivalent to [n * dotProduct(A,B) - sum(A) * sum(B)] / sqrt{ [n * norm(A)^2 - sum(A)^2] [n * norm(B)^2 - sum(B)^2] }
    *
    */
  def correlation(size: Double, dotProduct: Double, ratingSum: Double, rating2Sum: Double, ratingNormSeq: Double, rating2NormSeq: Double): Double = {
    val numerator = size * dotProduct - ratingSum * rating2Sum
    val denominator = math.sqrt(size * ratingNormSeq - ratingSum * ratingSum) * math.sqrt(size * rating2NormSeq - rating2Sum * rating2Sum)
    numerator / denominator
  }

  /**
    * Regularize correlation by adding virtual pseudocounts over a prior:
    * RegularizedCorrelation = w * ActualCorrelation + (1 - w) * PriorCorrelation
    * where w = # actualPairs / (# actualPairs + # virtualPairs).
    *
    */
  def regularizedCorrelation(size: Double, dotProduct: Double, ratingSum: Double, rating2Sum: Double, ratingNormSeq: Double, rating2NormSeq: Double,
                             virtualCount: Double, priorCorrelation: Double): Double = {
    val unregularizedCorrelation = correlation(size, dotProduct, ratingSum, rating2Sum, ratingNormSeq, rating2NormSeq)
    val w = size / (size + virtualCount)
    w * unregularizedCorrelation + (1 - w) * priorCorrelation
  }

  def cosineSimilarity(dotProduct: Double, ratingNorm: Double, rating2Norm: Double): Double = {
    dotProduct / (ratingNorm * rating2Norm)
  }

  def improvedCosineSimilarity(dotProduct: Double, ratingNorm: Double, rating2Norm: Double,
                               numAjoinB: Long, numA: Long, numB: Long): Double = {
    dotProduct * numAjoinB / (ratingNorm * rating2Norm * numA * math.log10(10 + numB))
  }

  def jaccardSimilarity(usersInCommon: Double, totalUsers1: Double, totalUsers2: Double): Double = {
    val union = totalUsers1 + totalUsers2 - usersInCommon
    usersInCommon / union
  }
}
