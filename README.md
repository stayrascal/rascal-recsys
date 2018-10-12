# Rascal-recsys
基于 solr 和协同过滤算法的博客检索与推荐系统

### Dependency
- HDFS
- HBase
- Phoenix
- Spark
- Kafka
- Zookeeper
- Yarn
- Solr
- hbase-indexer
- SpringBoot


### Environment Setup

#### Data Import
数据库定义文件位于data/example/sql目录下。数据文件位于data/example/data目录下
通过以下指令创建数据表并导入示例数据
```
psql.py jieba_dict.sql  recommend.sql  thesaurus.sql item.sql
psql.py -t JIEBA_DICT jieba_dict.csv
psql.py -t THESAURUS_GROUP thesaurus_group.csv
psql.py -t THESAURUS_BELONG thesaurus_belong.csv
psql.py -t USERS users.csv
psql.py -t COMPONENTS components.csv
psql.py -t HISTORY history.csv
```

#### solr 配置
- 将位于data/example/solr目录下的cn_schema_configs复制到${SOLR_HOME}/server/solr/configsets目录下
- 并将better-jieba-solr-1.0-SNAPSHOT.jar、phoenix-4.13.1-HBase-1.2-client.jar（位于phoenix安装目录下）复制到${SOLR_HOME}/server/solr-webapp/WEB-INF/lib/目录下
- 创建solr的zookeeper根路径 ```bin/solr zk mkroot /solr -z localhost:2181```
- 启动 solr 服务 ```bin/solr start -force -c -z localhost:2181/solr```
- 创建集合compsCollection并指定配置集 ```bin/solr create -force -c compsCollection -n compsCollConfigs -s 1 -rf 1 -d cn_schema_configs```

#### hbase-indexer 配置
- 将hbase-indexer-phoenix-mapper-1.0.0.jar和phoenix-core-4.13.1-HBase-1.2.jar（位于phoenix安装目录下）复制到${HBASE_INDEXER_HOME}\lib目录下
- 启动 hbase-indexer 服务 ```hbase-indexer server```
- 将data/example/hbase-indexer/morphlines.conf复制到/conf目录下，没有则创建
- 创建索引 ```hbase-indexer add-indexer -n compsindexer --indexer-conf morphline-phoenix-mapper.xml --connection-param solr.zk=localhost:2181/solr --connection-param solr.collection=compsCollection ```

### START STEPS
- Start HBase: start-hbase.sh
- Start HBase-indexer: hbase-indexer server
- Start Solr: solr start -force -c -z localhost:2181/solr