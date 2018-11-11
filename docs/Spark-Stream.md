## Spark Streaming消费kafka消息的两种方式对比
park streaming去消费kafka消息相信是一种比较常见的情景。一般来说获取kafka消息的spark streaming流有以下两种：
- 基于Receiver的流(传统老方法)：内存问题比较严重，因为她接受数据和处理数据是分开的。如果处理慢了，它还是不断的接受数据。容易把负责接受的节点给搞挂了。
- Direct Stream(推荐的新方法)：直接把 Kafka 的 partition 映射成 RDD 里的 partition 。 所以数据还是在 kafka 。只有在算的时候才会从 Kafka 里拿，不存在内存问题，速度也快。

### Receiver-based Approach
Receiver是利用kafka的high-level consumer API来实现的。接收器接受的kafka数据会存放到spark executors当中。然后spark streaming会处理这些数据。
#### 两种接收器
接收器根据是否需要返回消息接受确认，可以分为以下两种：
- Reliable Receiver: 例如kafka和flume都需要消费后的ack信息
- Unreliable Receiver: Spark Streaming处理kafka消息的API采用这种接收器，会导致消息丢失。

在Spark streaming中使用的是第二种接收器，为了防止消息丢失，可以开启Write Ahead Logs，然后异步保存kafka数据到这个写前日志中。这个Write Ahead Logs可以持久化存储到HDFS这种分布式存储上。通过这种方式可以避免数据丢失。
#### 其他注意点
- topic partitions: kafka分区和RDD的分区没关系。KafkaUtils.createStream() 中增加topic相关联的分区只能增加一个receiver当中的消费者线程。并不能提升SPARK 并行处理RDD的效率。
- 通过多个接收器可以，可以创建一个跨group接受多个topic数据的DStream

### Direct Approach (No Receivers)
这种方式按照batch来读取一定间隔的offset之间的所有消息，也就是DStream是offset一个一个移动的，频繁的修改offset。和前一种方法最大的区别，就是少了一个中间环节——receiver。
这种实现的好处：
- 简化并行的问题：现在不用像第一种方法来合并kafka stream流了。现在RDD的分区数和需要消费的分区数保持一致。方便调优了。也就是说提升并发消费的线程，也可以同时提升RDD处理能力。增加分区数和提升RDD并行处理能力、并发消费能力目标一致了。
- 高效： 现在的消息可靠性，可以利用Kafka本身的能力来恢复，因为已经没有中间环节——接收器了。这样也就不需要做两次持久化，也不需要Write Ahead logs了，性能好了不少。
- Exactly-once semantics: Spark streaming和Zookeeper都会记录offset。但是这两者可能保持不一致，从而导致没法做到有且仅有一次的语义。现在这种“直接流”的方式直接不使用ZK来管理offset，而只交给SS来管理，这样避免不一致的问题。当然这样也会有缺点，如果你是监控ZK上的offset来了解消费情况的，那么现在就没法获取到了。当然解决办法也有，就是你SS处理好了，提交OFFSET的同时，也去写一下ZK。

#### Direct Approach的好处
- 按需拉数据，不存在缓冲区，就不用担心缓冲区把内存撑爆了。这个在Receiver-based Approach 就比较麻烦，你需要通过spark.streaming.blockInterval等参数来调整。
- 数据默认就被分布到了多个Executor上。Receiver-based Approach 你需要做特定的处理，才能让 Receiver分到多个Executor上。
- Receiver-based Approach 的方式，一旦你的Batch Processing 被delay了，或者被delay了很多个batch,那估计你的Spark Streaming程序离奔溃也就不远了。 Direct Approach (No Receivers) 则完全不会存在类似问题。就算你delay了很多个batch time,你内存中的数据只有这次处理的。
- Direct Approach (No Receivers) 直接维护了 Kafka offset,可以保证数据只有被执行成功了，才会被记录下来，透过 checkpoint机制。如果采用Receiver-based Approach，消费Kafka和数据处理是被分开的，这样就很不好做容错机制，比如系统宕掉了。所以你需要开启WAL,但是开启WAL带来一个问题是，数据量很大，对HDFS是个很大的负担，而且也会对实时程序带来比较大延迟。

#### 限速
- 在Direct Approach中，可以通过参数 spark.streaming.kafka.maxRatePerPartition 来配置的。只是需要注意的是，这里是对每个Partition进行限速。所以你需要事先知道Kafka有多少个分区，才好评估系统的实际吞吐量，从而设置该值。也可以打开spark.streaming.backpressure.enabled根据processing time来动态调整
- 在Receiver-based Approach 中，可以配置spark.streaming.receiver.maxRate参数来设置currentBuffer的上线填充速度，也可以打开spark.streaming.backpressure.enabled根据processing time来动态调整。

## At least once
SS 是靠CheckPoint 机制 来保证 at least once 语义的。
### CheckPoint 机制
- checkpoint 是非常高效的。没有涉及到实际数据的存储。一般大小只有几十K，因为只存了Kafka的偏移量等信息。
- checkpoint 采用的是序列化机制，尤其是DStreamGraph的引入，里面包含了可能如ForeachRDD等，而ForeachRDD里面的函数应该也会被序列化。如果采用了CheckPoint机制，而你的程序包做了做了变更，恢复后可能会有一定的问题。
### JobGenerator提交任务过程
- 产生jobs
    * 成功则提交jobs 然后异步执行
    * 失败则会发出一个失败的事件
- 无论成功或者失败，都会发出一个 DoCheckpoint 事件。
- 当任务运行完成后，还会再调用一次DoCheckpoint 事件。
只要任务运行完成后没能顺利执行完DoCheckpoint前crash,都会导致这次Batch被重新调度。也就说无论怎样，不存在丢数据的问题，而这种稳定性是靠checkpoint 机制以及Kafka的可回溯性来完成的。

## Exactly Once
要保证Exactly Once，需要有一个前提，就是我们的业务操作要满足下面两个条件：
- 幂等操作：这种场景不用做额外的工作
- 业务代码需要自身添加事物操作：这种场景只能通过自身的业务代码逻辑实现
    ```dstream.foreachRDD { (rdd, time) =>
         rdd.foreachPartition { partitionIterator =>
           val partitionId = TaskContext.get.partitionId()
           val uniqueId = generateUniqueId(time.milliseconds, partitionId)
           // use this uniqueId to transactionally commit the data in partitionIterator
         }
       }
    ```

## Spark Streaming与Storm使用场景分析
- Storm写数据到HDFS比较麻烦：
    *  Storm 需要持有大量的 HDFS 文件句柄。需要落到同一个文件里的记录是不确定什么时候会来的，你不能写一条就关掉，所以需要一直持有。
    * 需要使用HDFS 的写文件的 append 模式，不断追加记录。