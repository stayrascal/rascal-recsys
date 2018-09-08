package com.stayrascal.spark.example.mllib

import org.apache.spark.mllib.fpm.AssociationRules
import org.apache.spark.mllib.fpm.FPGrowth.FreqItemset
import org.apache.spark.sql.SparkSession

object AssociationRulesExample {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .appName("AssociationRulesExample")
      .master("local[*]")
      .getOrCreate()

    val freqItemsets = spark.sparkContext.parallelize(Seq(
      new FreqItemset(Array("a"), 15L),
      new FreqItemset(Array("b"), 35L),
      new FreqItemset(Array("a", "b"), 12L)
    ))

    val ar = new AssociationRules()
      .setMinConfidence(0.8)
    val results = ar.run(freqItemsets)

    results.collect().foreach { rule =>
      println("[" + rule.antecedent.mkString(",")
        + "=>"
        + rule.consequent.mkString(",") + "]," + rule.confidence)
    }
  }

}
