val springVersion = "1.5.15.RELEASE"

libraryDependencies ++= Seq(
  "org.springframework.boot" % "spring-boot-starter-web" % springVersion,
  "org.springframework.boot" % "spring-boot-configuration-processor" % springVersion,
  "org.springframework.data" % "spring-data-hadoop" % "2.5.0.RELEASE",
  "org.mybatis.spring.boot" % "mybatis-spring-boot-starter" % "1.3.2",
"org.apache.solr" % "solr-solrj" % "7.4.0",
  "org.apache.phoenix" % "phoenix-spark" % "4.14.0-HBase-1.2",
  "org.apache.hbase" % "hbase-client" % "1.2.6",
  "org.apache.hbase" % "hbase-common" % "1.2.6",
  "io.reactivex.rxjava2" % "rxjava" % "2.0.1",
  "org.jsoup" % "jsoup" % "1.11.3",
  "org.apache.zookeeper" % "zookeeper" % "3.4.13"
//  "io.reactivex" % "rxjava" % "1.3.8"
//  "org.apache.hadoop" % "hadoop-client" % "2.7.7"
)