## Kafka关键概念介绍


### Producer的策略

#### 是否ACK
所谓ACK，是指服务器收到消息之后，是存下来之后，再给客户端返回，还是直接返回。很显然，是否ACK，是影响性能的一个重要指标。在kafka中，request.required.acks有3个取值，分别对应3种策略：
- 0: 不等服务器ack就返回了，性能最高，可能丢数据 
- 1: leader确认消息存下来了，再返回 
- all: leader和当前ISR中所有replica都确认消息存下来了，再返回（这种方式最可靠）

#### 同步发送 vs 异步发送
所谓异步发送，就是指客户端有个本地缓冲区，消息先存放到本地缓冲区，然后有后台线程来发送。异步发送可以提高发送的性能，但一旦客户端挂了，就可能丢数据。对于RabbitMQ, ActiveMQ，他们都强调可靠性，因此不允许非ACK的发送，也没有异步发送模式。Kafka提供了这个灵活性，允许使用者在性能与可靠性之间做权衡。同步发送其实是由异步发送实现的，实现同步发送，只需要拿到Future之后，调用get方法就可以了。

异步发送的基本思路就是：send的时候，KafkaProducer把消息放到本地的消息队列RecordAccumulator，然后一个后台线程Sender不断循环，把消息发给Kafka集群。要实现这个，还得有一个前提条件：就是KafkaProducer/Sender都需要获取集群的配置信息Metadata。Metadata主要有一个记录了集群信息的Cluster对象和一堆控制Metadata更新策略的转态变量组成，里面存储有Topic/Partition和broker的映射关系，针对每一个topic的partition，需要知道其对应的broke列表是什么，leader是谁，follower是谁。

RecordAccumulator最大的一个特性就是batch消息，扔到队列中的多个消息，可能组成一个RecordBatch，然后由Sender一次性发送出去。每个TopicPartition对应一个消息队列，只有同一个TopicPartition的消息，才可能被batch。消息队列用的是一个“双端队列“，而不是普通的队列。是为了处理“发送失败，重试“的问题：当消息发送失败，要重发的时候，需要把消息优先放入队列头部重新发送，这就需要用到双端队列，在头部，而不是尾部加入。

详细流程为sender从集群获取信息，然后更新Metadata，Producer先读取Metadata，然后将消息放入队列，Sender消费队列中的消息发送给集群。

Sender和服务器通信的时候，会调动KafkaClient的方法，KafkaClient是基于Java NIO封装的的一个网络层

### NIO四大组件
- Channel: 在通常的Java网络编程中，我们知道有一对Socket/ServerSocket对象，每1个socket对象表示一个connection，ServerSocket用于服务器监听新的连接。在NIO中，与之相对应的一对是SocketChannel/ServerSocketChannel。一个Channel最基本的操作就是read/write，并且其传进去的必须是ByteBuffer类型，而不是普通的内存buffer。
- Buffer: 用来封装channel发送／接收的数据。
- Selector: 主要目的是网络事件的 loop 循环，通过调用selector.poll，不断轮询每个Channel上读写事件
- SelectionKey: 用来记录一个Channel上的事件集合，每个Channel对应一个SelectionKey。SelectionKey也是Selector和Channel之间的关联，通过SelectionKey可以取到对应的Selector和Channel。

### 四种网络IO模型
- epoll与IOCP
    - 阻塞IO：read/write的时候，阻塞调用
    - 非阻塞IO：read/write，没有数据，立马返回，轮询
    - IO复用：read/write一次都只能监听一个socket，但对于服务器来讲，有成千上完个socket连接，如何用一个函数，可以监听所有的socket上面的读写事件呢？这就是IO复用模型，对应linux上面，就是select/poll/epoll3种技术。
    - 异步IO：linux上没有，windows上对应的是IOCP。
- 两种网络IO的设计模式
    - Reactor模式：主动模式，所谓主动，是指应用程序不断去轮询，问操作系统，IO是否就绪。Linux下的select/poll/epooll就属于主动模式，需要应用程序中有个循环，一直去poll。 在这种模式下，实际的IO操作还是应用程序做的。
    - Preactor模式：被动模式，你把read/write全部交给操作系统，实际的IO操作由操作系统完成，完成之后，再callback你的应用程序。Windows下的IOCP就属于这种模式，再比如C++ Boost中的Asio库，就是典型的Preactor模式。
- epoll编程模型的三个阶段
    - 在Linux平台上，Java NIO就是基于epoll来实现的。所有基于epoll的框架，都有3个阶段： 
      注册事件(connect,accept,read,write)， 轮询IO是否就绪，执行实际IO操作。
    - 同样， NIO中的Selector同样也有这3个阶段
- epoll和selector在注册上的差别-LT&ET模式
    - epoll里面有2种模式：LT(水平触发）和 ET(边缘触发）。水平触发，又叫条件触发；边缘触发，又叫状态触发。
        - 水平触发(条件触发）：读缓冲区只要不为空，就一直会触发读事件；写缓冲区只要不满，就一直会触发写事件。
        - 边缘触发(状态触发）：读缓冲区的状态，从空转为非空的时候，触发1次；写缓冲区的状态，从满转为非满的时候，触发1次。
        - LT适用于阻塞和非阻塞IO, ET只适用于非阻塞IO。
        - Java NIO用的就是epoll的LT模式

### Consumer消费策略

#### Consumer Group – 负载均衡模式 vs. Pub/Sub模式
每一个consumer实例，在初始化的时候，都需要传一个group.id，这个group.id决定了多个Consumer在消费同一个topic的时候，是分摊，还是广播。假设多个Consumer都订阅了同一个topic，这个topic有多个partition.
- 负载均衡模式： 多个Consumer属于同一个group，则topic对应的partition的消息会分摊到这些Consumer上。(也可以使用assign函数强制指定consumer消费哪个topic的那个partition)
- Pub/Sub模式：多个Consumer属于不同的group，则这个topic的所有消息，会广播到每一个group。

#### 消费确认 - consume offset vs. committed offset
- 一个是当前取消息所在的consume offset
- 一个是处理完毕，发送ack之后所确定的committed offset。
- 假如consumer挂了重启，那它将从committed offset位置开始重新消费，而不是consume offset位置。这也就意味着有可能重复消费。
- consumer有3中ACK策略
    - 自动的，周期性的ack。
    - consumer.commitSync() //调用commitSync，手动同步ack。每处理完1条消息，commitSync 1次
    - consumer. commitASync() //手动异步ack

#### Exactly Once – 自己保存offset
- Kafka只保证消息不漏，即at lease once，而不保证消息不重。
- Consumer端：
    - 禁用自动ack
    - 每次取到消息，把对应的offset存下来
    - 下次重启，通过consumer.seek函数，定位到自己保存的offset，从那开始消费
