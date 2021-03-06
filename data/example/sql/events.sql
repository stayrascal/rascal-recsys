DROP TABLE IF EXISTS events;
DROP LOCAL INDEX IF EXISTS events_idx ON events;
DROP SEQUENCE IF EXISTS events_seq;
DROP LOCAL TABLE IF EXISTS query_logs;
DROP INDEX IF EXISTS query_logs_idx ON query_logs;

CREATE TABLE events(
id BIGINT PRIMARY KEY,
INFO.userId BIGINT,
INFO.itemId BIGINT,
INFO.action VARCHAR(10),
INFO.createTime TIME
) REPLICATION_SCOPE=1, SALT_BUCKETS = 3, COLUMN_ENCODED_BYTES = 0, COMPRESSION='GZ';
CREATE LOCAL INDEX events_idx ON events(INFO.userId);
CREATE SEQUENCE events_seq START 0 INCREMENT BY 1 CACHE 10;

CREATE TABLE query_logs(
id BIGINT PRIMARY KEY,
INFO.userId BIGINT,
INFO.query VARCHAR,
INFO.resultCnt INTEGER,
INFO.clickItemId BIGINT,
INFO.createTime TIME
) REPLICATION_SCOPE=1, SALT_BUCKETS = 3, COLUMN_ENCODED_BYTES = 0, COMPRESSION='GZ';
CREATE LOCAL INDEX query_logs_idx ON query_logs(INFO.userId);
CREATE SEQUENCE query_logs_seq START 0 INCREMENT BY 1 CACHE 10;

CREATE TABLE recommend_logs(
userId BIGINT NOT NULL,
clickItemId BIGINT NOT NULL,
otherItems VARCHAR NOT NULL,
createTime TIME,
CONSTRAINT pk PRIMARY KEY (userId, clickItemId, otherItems)
) REPLICATION_SCOPE=1, SALT_BUCKETS = 3, COLUMN_ENCODED_BYTES = 0, COMPRESSION='GZ';
CREATE LOCAL INDEX recommend_logs_idx ON recommend_logs(userId);