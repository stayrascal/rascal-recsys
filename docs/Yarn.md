
### ApplicationMaster管理
ApplicationMaster管理涉及到了4大类,ApplicationMasterLauncher,AMLivelinessMonitor,ApplicationMasterService,以及ApplicationMaster自身类.在Yarn中,每个类都会有自己明确的功能模块的区分.
- ApplicationMasterLauncher--姑且叫做AM启动关闭事件处理器,他既是一个服务也是一个处理器,在这个类中,只处理2类事件,launch和cleanup事件.分别对应启动应用和关闭应用的情形.
- AMLivelinessMonitor--这个类从名字上可以看出他是监控类,监控的对象是AM存活状态的监控类,检测的方法与之前的HDFS一样,都是采用heartbeat的方式,如果有节点过期了,将会触发一次过期事件.
- ApplicationMasterService--AM请求服务处理类.AMS存在于ResourceManager,中,服务的对象是各个节点上的ApplicationMaster,负责接收各个AM的注册请求,更新心跳包信息等.
- ApplicationMaster--节点应用管理类,简单的说,ApplicationMaster负责管理整个应用的生命周期.
- 执行顺序
    - 用户提交了新的Application的时候,ApplicationMasterLauncher会生成Launch事件,然后调用RPC函数与远程的NodeManager通信,让其准备启动的新的Container来启动AM. 
    - ApplicationMasterLauncher同时将AM注册到AMLiveLinessMonitor中
    - AM在启动时,会被执行run()方法,在run()方法中会与ResourceManager通信，周期性的发送心跳信息.
    - AM启动结束后调用ApplicationMasterService的registerApplicationMaster方法将AM注册到ResourceManager
    - AMS每次收到AM的信息信息时,都会在AMLivelinessMonitor上更新最新时间.
    - AM运行结束或者异常结束后,AMLivelinessMonitor中的CheckThread会检查心跳监控中出现过期的现象，触发对应的expire事件，然后发给中央处理器去处理

### NodeManager管理
- NodeManager.java(yarn-resourcemanager)--节点管理类
- NodesListManager--节点列表管理类，这个类中管理了类似黑名单，白名单的节点列表形式。
- NMLivelinessMonitor--节点存活状态监控线程类，与之前的AMLivelinessMonitor线程的原理类似，最简单的心跳更新检查。
- ResourceTrackerService--节点服务管理对象，负责与各个NodeManager通信。包括NM在此服务上的注册请求处理，心跳更新操作等等。
- 执行顺序
    - 构造新的NodeManager的时候，调用ResourceTrackerService的registerNodeManager将注册对应node,ResourceTrackerService类NodesListManager和NMLivelinessMonitor
    - ResourceTrackerService会检查节点是否有效,同时检查节点资源是否满足最小内存和核数的限制
    - ResourceTrackerService将满足条件的注册节点加入到NMLivelinessMonitor线程中
    - NodeManager创建后，会周期性的调用ResourceTrackerService的nodeHeartbeat方法发送心跳信息
    - ResourceTrackerService检查发送心跳信息的节点是否存在，然后更新心跳时间。每次心跳检测时都会检查该节点是否被拉入exclude名单，然后在设置心跳回复
    
### RM应用状态信息保存
- MemoryRMStateStore--信息状态保存在内存中的实现类。
- FileSystemRMStateStore--信息状态保存在HDFS文件系统中，这个是做了持久化了。
- NullRMStateStore--do nothing，什么都不做，就是不保存应用状态信息。
- ZKRMStateStore--信息状态保存在Zookeeper中。

### JournalNode
JournalNode这个概念是在MR2也就是Yarn中新加的,journalNode的作用是存放EditLog的,在MR1中editlog是和fsimage存放在一起的然后SecondNamenode做定期合并
JournalNode处于Active Namenode与StandBy Namenode之间，数量不一定只有1个,作用相当于NFS共享文件系统.Active Namenode往里写editlog数据,StandBy再从里面读取数据进行同步.
但有多个JournalNode时，一定会存在一个类似管理者这样的角色存在,而这个管理者就是QJM,全程QuorumJournalManager.

### Ganglia
Ganglia的架构设计也是类似于Client-Server的模式,Client端会开启gmond进程进行客户端监控数据的收集,然后发给Server端,server端对数据进行收集并进行页面的展示。
Ganglia对Hadoop进行了很完美的支持,Hadoop在Ganglia上定义了非常多细粒度的指标,基本涉及了非常多方面的统计信息.

  
