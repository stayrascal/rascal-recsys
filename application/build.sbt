val springVersion = "1.5.15.RELEASE"
val sparkVersion = "2.2.0"

libraryDependencies ++= Seq(
  "org.springframework.boot" % "spring-boot-starter-web" % springVersion,
  "org.springframework.boot" % "spring-boot-configuration-processor" % springVersion,
  "org.springframework.data" % "spring-data-hadoop" % "2.5.0.RELEASE",
  "org.mybatis.spring.boot" % "mybatis-spring-boot-starter" % "1.3.2",
  "org.apache.solr" % "solr-solrj" % "7.4.0",

  "org.apache.phoenix" % "phoenix-spark" % "4.14.0-HBase-1.2",
  "c3p0" % "c3p0" % "0.9.1.2",
  "org.apache.hbase" % "hbase-client" % "1.2.6",
  "org.apache.hbase" % "hbase-common" % "1.2.6",

  "io.reactivex.rxjava2" % "rxjava" % "2.0.1",
  "org.jsoup" % "jsoup" % "1.11.3",
  "org.apache.zookeeper" % "zookeeper" % "3.4.13",
  "com.esotericsoftware" % "kryo" % "4.0.2",
  //  "io.reactivex" % "rxjava" % "1.3.8"
  //  "org.apache.hadoop" % "hadoop-client" % "2.7.7"


  "org.apache.spark" %% "spark-core" % sparkVersion % "provided",
  "org.apache.spark" %% "spark-sql" % sparkVersion % "provided",
  "org.apache.spark" %% "spark-mllib" % sparkVersion,
  "org.apache.spark" %% "spark-streaming" % sparkVersion % "provided",
  "org.apache.spark" %% "spark-hive" % sparkVersion,
  "org.apache.spark" %% "spark-streaming-kafka-0-10" % sparkVersion % "provided",
//  "org.apache.spark" %% "spark-streaming-kafka" % "1.6.3" % "provided",
  "org.apache.kafka" % "kafka-streams" % "1.1.0"
)