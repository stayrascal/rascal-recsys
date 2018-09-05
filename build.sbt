

name := "rascal-recsys"
version := "0.0.1"
scalaVersion := "2.11.11"

val sparkVersion = "2.2.0"
val kafkaVersion = "1.1.1"
val jettyVersion = "9.4.12.v20180830"
val containerVersion = "2.25.1"

libraryDependencies ++= Seq(
  "javax.ws.rs" % "javax.ws.rs-api" % "2.0",

  "org.apache.spark" %% "spark-core" % sparkVersion,
  "org.apache.spark" %% "spark-sql" % sparkVersion,
  "org.apache.spark" %% "spark-mllib" % sparkVersion,
  "org.apache.spark" %% "spark-streaming" % sparkVersion,
  "org.apache.spark" %% "spark-hive" % sparkVersion,
  "org.apache.kafka" %% "kafka" % kafkaVersion,

  "org.apache.spark" %% "spark-streaming-kafka" % "1.6.3",
  "org.apache.mahout" % "mahout-mr" % "0.13.0",
  "redis.clients" % "jedis" % "2.9.0",
  "com.alibaba" % "fastjson" % "1.2.49",

  "org.eclipse.jetty" % "jetty-server" % jettyVersion,
  "org.eclipse.jetty" % "jetty-servlet" % jettyVersion,
  "org.eclipse.jetty" % "jetty-util" % jettyVersion,

  "org.glassfish.jersey.core" % "jersey-server" % containerVersion,
  "org.glassfish.jersey.containers" % "jersey-container-servlet-core" % containerVersion,
  "org.glassfish.jersey.containers" % "jersey-container-jetty-http" % containerVersion,
  "org.glassfish.jersey.media" % "jersey-media-moxy" % containerVersion

)