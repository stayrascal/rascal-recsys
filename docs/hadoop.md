

### hadoop的map-reduce编程模型
- Map Task
    1. Map task使用Hadoop内置的数据类型(longwritable、text等)从文件系统中读取数据，转为Key-Value形式的键值对集合
        - 转换数据时，会在Map之前使用InputFormat对数据进行两方面的预处理
            - 一是getSplits，返回的是InputSplit数组，对数据进行split分片，每片交给map操作一次
            - 二是getRecordReader，返回的是RecordReader对象，对每个split分片进行转换为key-value键值对格式传递给map
        -  常用的InputFormat是TextInputFormat，使用的是LineRecordReader对每个分片进行键值对的转换，以行偏移量作为键，行内容作为值
        - 可以自定义类继承InputFormat接口，重写createRecordReader和isSplitable方法，在createRecordReader中可以自定义分隔符
    2. 用Mapper中的业务处理逻辑对将上面的键值对集合进行处理，得到需要的Key-Value键值对集合
    3. 对上述的键值对集合进行分区(partition)操作，默认使用的是hashpartitioner，可以通过重写hashpartitioner的getpartition方法来自定义分区规则
    4. 对key进行排序(sort)，然后分组(grouping)将相同key的value合并分组输出，可以使用自定义的数据类型，重写WritableComparator的Comparator方法来自定义排序规则，重写RawComparator的compare方法来自定义分组规则
    5. 对上面结果进行归约(combiner)操作，通过一个本地段的reduce预处理，以减小后面shufle和reducer的工作量 
- Reduce Task
    1. reduce task会通过网络将各个数据收集进行reduce处理，最后将数据保存或者显示，结束整个job
    
### hadoop和spark的相同点和区别
- 相同点
    - 两者都是用mr模型来进行并行计算
- 区别
    - 任务运行的角度
        - hadoop的一个作业称为job，job里面分为map task和reduce task，每个task都是在自己的进程中运行的，当task结束时，进程也会结束
        - spark用户提交的任务称为application，一个application对应一个sparkcontext，application中存在多个job，每触发一次action操作就会产生一个job。这些job可以并行或串行执行，每个job中有多个stage，stage是shuffle过程中DAGSchaduler通过RDD之间的依赖关系划分job而来的，每个stage里面有多个task，组成taskset，由TaskSchaduler分发到各个executor中执行，executor的生命周期是和application一样的，即使没有job运行也是存在的，所以task可以快速启动读取内存进行计算
    - 功能角度 
        - hadoop的job只有map和reduce操作，表达能力比较欠缺，而且在mr过程中会重复的读写hdfs，造成大量的io操作，多个job需要自己管理关系
        - spark的迭代计算都是在内存中进行的，API中提供了大量的RDD操作如join，groupby等，而且通过DAG图可以实现良好的容错

### Flume工作机制
1. Flume核心概念是agent，里面包括source、chanel和sink三个组件。
2. source运行在日志收集节点进行日志采集，之后临时存储在chanel中，sink负责将chanel中的数据发送到目的地。只有成功发送之后chanel中的数据才会被删除。
3. 开发时，先书写flume配置文件，定义agent、source、chanel和sink然后将其组装，执行flume-ng命令。

### Sqoop工作原理
1. Sqoop是hadoop生态圈上的数据传输工具。可以将关系型数据库的数据导入非结构化的hdfs、hive或者hbase中，也可以将hdfs中的数据导出到关系型数据库或者文本文件中。
2. 主要使用的是mr程序来执行任务，使用jdbc和关系型数据库进行交互。 
3. import原理：通过指定的分隔符进行数据切分，将分片传入各个map中，在map任务中在每行数据进行写入处理没有reduce。 
4. export原理：根据要操作的表名生成一个java类，并读取其元数据信息和分隔符对非结构化的数据进行匹配，多个map作业同时执行写入关系型数据库

### Hbase行健列族的概念，物理模型，表的设计原则
1. 行键是HBase表自带的，每个行健对应一条数据。
2. 列簇是创建表时指定的，为列的集合，每个列族作为一个文件单独存储，存储的数据都是字节数组，其中的数据可以有很多，通过时间戳来区分。 
3. 从物理模型设计上看，整个hbase表会拆分为多个region，每个region记录着行健的起始点保存在不同的节点上，查询时就是对各个节点的并行查询，当region很大时使用.META表存储各个region的起始点，-ROOT又可以存储.META的起始点。
4. RowKey的设计原则：
    - 各个列簇数据平衡
    - 长度原则
    - 相邻原则
    - 创建表的时候设置表放入regionserver缓存中
    - 避免自动增长和时间
    - 使用字节数组代替string，最大长度64kb，最好16字节以内
    - 按天分表，两个字节散列，四个字节存储时分毫秒。
5. 列簇的设计原则：
    - 尽可能少（按照列族进行存储，按照region进行读取，不必要的io操作）
    - 经常和不经常使用的两类数据放入不同列族中，列族名字尽可能短。

### Hadoop性能调优
调优可以通过系统配置、程序编写和作业调度算法来进行。
1. 在网络很好的情况下，可以将hdfs的block.size可以调到128/256（默认为64）
2. 主要调优的方式：mapred.map.tasks、mapred.reduce.tasks设置mr任务数（默认都是1）
    - mapred.tasktracker.map.tasks.maximum每台机器上的最大map任务数 
    - mapred.tasktracker.reduce.tasks.maximum每台机器上的最大reduce任务数 
    - mapred.reduce.slowstart.completed.maps配置reduce任务在map任务完成到百分之几的时候开始进入 
    - mapred.compress.map.output,mapred.output.compress配置压缩项，消耗cpu提升网络和磁盘io
3. 合理利用combiner
4. 注意重用writable对象

### HDFS的文件权限
- HDFS的文件和目录权限模型共享了POSIX的很多部分，与POSIX模型不同的是，HDFS中的文件没有可执行文件的概念，粘贴位（sticky bit）可以用在目录上，用于阻止除超级用户，目录或文件的拥有者外的任何删除或移动目录中的文件，文件上的粘贴位不起作用
- 当创建文件或目录时，拥有者为运行客户端进程的用户，组为父目录所属的组。无论何时HDFS必须对由客户端进程访问的文件或目录进行权限检查，规则如下：
    - 如果进程的用户名匹配文件或目录的拥有者，那么测试拥有者权限
    - 否则如果文件或目录所属的组匹配组列表中任何组，那么测试组权限
    - 否则测试其它权限
- 用户和用户组
    - 从hadoop-0.22开始，hadoop支持两种不同的操作模式以确定用户，分别为simple和kerberos。具体使用哪个方式由参数hadoop.security.authentication设置，默认为simple。无论哪种操作模式，对于HDFS来说用户标识机制都是外部的，HDFS本身没有创建用户标，建立组或者处理用户凭证的规定。
    - 用户组是通过由参数hadoop.security.group.mapping设置的组映射服务确定的。
        - 默认实现是org.apache.hadoop.security.JniBasedUnixGroupsMappingWithFallback，该实现首先确定Java本地接口（JNI）是否可用，如果JNI可用，该实现将使用hadoop中的API为用户解析用户组列表。
        - 如果JNI不可用，那么使用ShellBasedUnixGroupsMapping，该实现将使用Linux/Unix中的bash –cgroups命令为用户解析用户组列表。
        - 其它实现还有LdapGroupsMapping，通过直接连接LDAP服务器来解析用户组列表。
        - 对HDFS来说，用户到组的映射是在NameNode上执行的，因而NameNode的主机系统配置决定了用户的组映射。HDFS将文件或目录的用户和组存储为字符串，并且不像Linux/Unix那样可以将用户和组转换为数字。
    - 每个针对文件或者目录的操作都将全路径名称传递到NameNode，然后对该路径的每次操作都将应用权限检查。
    - 超级用户：
        -  HDFS中超级用户与通常熟悉的Linux或Unix中的root用户不同，HDFS的超级用户是与NameNode进程有相同标示的用户，更简单易懂些，启动NameNode的用户就为超级用户。
        - HDFS的超级用户不必是NameNode主机的超级用户，也需用所有的集群使用相同的超级用户。
        - 另外参数dfs.permissions.superusergroup设置了超级用户，该组中的所有用户也为超级用户。
        - 超级用户在HDFS中可以执行任何操作而针对超级用户的权限检查永远不会失败。
- ACL:
    - HDFS也提供了对POSIX ACL（访问控制列表）支持来为特定的用户或者用户组提供更加细粒度的文件权限
    - ACL是不同于用户和组的自然组织层次的有用的权限控制方式，ACL可以为特定的用户和组设置不同的权限，而不仅仅是文件的拥有者和文件所属的组。
    - 默认情况下，HDFS禁用ACL，因此NameNode禁止ACL的创建，为了启用ACL，需要在hdfs-site.xml中将参数dfs.namenode.acls.enabled设置为true。
    - 每个ACL项由类型，可选的名称和权限字符串组成，它们之间使用冒号:分割
        ```user::rw-
           user::rw-
           user:bruce:rwx
           group::r-x
           group:sales:rwx
           mask::r--
           other::r--
        ```
    - 在上面的例子中文件的拥有者具有读写权限，文件所属的组具有读和执行的权限，其他用户具有读权限，这些设置与将文件设置为654等价，除此之外，还有两个扩展的ACL项，分别为用户bruce和组sales，并都授予了读写和执行的权限。
    - mask项是一个特殊的项，用于过滤授予所有命名用户，命名组及未命名组的权限，即过滤除文件拥有者和其他用户(other)之外的任何ACL项。在该例子中，mask值有读权限，则bruce用户、sales组和文件所属的组只具有读权限。
    - 每个ACL必须有mask项，如果用户在设置ACL时没有使用mask项，一个mask项被自动加入到ACL中，该mask项是通过计算所有被mask过滤项的权限与（&运算）得出的。
    - 访问控制列表和默认访问控制列表 AclEntryScope
        - 访问控制列表(Access)定义了在执行权限检查实施的规则
        - 默认访问控制列表(DEFAULT)定义了新文件或者子目录创建时自动接收的ACL项
            - 只有目录可能拥有默认访问控制列表，当创建新文件或者子目录时，自动拷贝父辈的默认访问控制列表到自己的访问控制列表中，新的子目录也拷贝父辈默认的访问控制列表到自己的默认访问控制列表中。
            - 拷贝ACL发生在文件或子目录的创建时，后面如果修改父辈的默认ACL将不再影响已存在子类的ACL。
    - 当一个文件拥有ACL时，权限检查的算法变为：
        - 如果用户名匹配文件的拥有者，则测试拥有者权限
        - 否则，如果用户名匹配命名用户项中的用户名，则测试由mask权限过滤后的该项的权限
        - 否则，如果文件所属的组匹配组列表中的任何组，并且如果这些被mask过滤的权限具有访问权限，那么使用这么权限
        - 否则，如果存在命名组项匹配组列表中的成员，并且如果这些被mask过滤的权限具有访问权限，那么使用这么权限
        - 否则，如果文件所属的组或者任何命名组项匹配组列表中的成员，但不具备访问权限，那么访问被拒绝
        - 否则测试文件的其他用户权限
        

### Spark基本概念与执行流程
1. 基本概念
    - Application：表示你的应用程序
    - Driver：表示main()函数，创建SparkContext。由SparkContext负责与ClusterManager通信，进行资源的申请，任务的分配和监控等。程序执行完毕后关闭SparkContext
    - Executor：某个Application运行在Worker节点上的一个进程，该进程负责运行某些task，并且负责将数据存在内存或者磁盘上。在Spark on Yarn模式下，其进程名称为CoarseGrainedExecutorBackend，一个CoarseGrainedExecutorBackend进程有且仅有一个executor对象，它负责将Task包装成taskRunner，并从线程池中抽取出一个空闲线程运行Task，这样，每个CoarseGrainedExecutorBackend能并行运行Task的数据就取决于分配给它的CPU的个数。
    - Worker：集群中可以运行Application代码的节点。在Standalone模式中指的是通过slave文件配置的worker节点，在Spark on Yarn模式中指的就是NodeManager节点。
    - Task：在Executor进程中执行任务的工作单元，多个Task组成一个Stage
    - Job：包含多个Task组成的并行计算，是由Action行为触发的
    - Stage：每个Job会被拆分很多组Task，作为一个TaskSet，其名称为Stage
    - DAGScheduler：根据Job构建基于Stage的DAG，并提交Stage给TaskScheduler，其划分Stage的依据是RDD之间的依赖关系
    - TaskScheduler：将TaskSet提交给Worker（集群）运行，每个Executor运行什么Task就是在此处分配的。
2. 执行流程
    - 用户在Client端提交作业后，会由Driver运行main方法并创建spark context上下文。
    - 执行RDD算子，形成DAG图输入DAGScheduler，按照RDD之间的依赖关系划分stage输入task scheduler。
        - Spark在进行transformation计算的时候，不会触发Job ，只有执行action操作的时候，才会触发Job
        - 在Driver中SparkContext根据RDD之间的依赖关系创建出DAG有向无环图，DAGScheduler负责解析这个图，解析时是以Shuffle为边界，反向解析，构建stage。
    - TaskScheduler会将stage划分为task set分发到各个节点的executor中执行。
        - 将多个任务根据依赖关系划分为不同的Stage，将每个Stage的Taste Set 提交给TaskScheduler去执行
        - 任务会在Executor进程的多个Task线程上执行，完成Task任务后 将结果信息提交到ExecutorBackend中 他会将信息提交给TaskScheduler。
        - TaskScheduler接到消息后通知TaskManager，移除该Task任务，开始执行下一个任务。
        - TaskScheduler同时会将信息同步到TaskSet Manager中一份，全部任务执行完毕后TaskSet Manager将结果反馈给DAGScheduler，如果属于ResultTask 会交给JobListener。否则话全部任务执行完毕后写入数据。

### Spark的优化
调优可以通过系统配置、程序编写和作业调度算法来进行。
1. 通过spark-env文件、程序中SparkConf和set property设置。
    - 计算量大，形成的lineage过大应该给已经缓存了的rdd添加checkpoint，以减少容错带来的开销。 
    - 小分区合并，过小的分区造成过多的切换任务开销，使用repartition。
    
### Hive UDF VS UDAF VS UDTF
- UDF
    - 自定义UDF需要继承org.apache.hadoop.hive.ql.UDF
    - UDF只能实现一进一出的操作，如果需要实现多进一出，则需要实现UDAF
- UDAF
    - 函数类需要继承UDAF类，内部类Evaluator实UDAFEvaluator接口。
    - UDF是基于单条记录的列进行的计算操作，而UDFA则是用户自定义的聚类函数，是基于表的所有记录进行的计算操作。
- UDTF: User-Defined Table-Generating Functions
    - UDTF用来解决输入一行输出多行(On-to-many maping)的需求。
    - 继承GenericUDTF
    - UDTF有两种使用方法，一种直接放到select后面，一种和lateral view一起使用。   