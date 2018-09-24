DROP TABLE IF EXISTS items;
DROP SEQUENCE IF EXISTS items_seq;
DROP INDEX IF EXISTS items_idx;

create table items
(uuid VARCHAR PRIMARY KEY,
info.id BIGINT,
info.title VARCHAR,
info.content VARCHAR,
info.describe VARCHAR,
info.link VARCHAR,
info.tags VARCHAR)
REPLICATION_SCOPE = 1,
SALT_BUCKETS = 3,
COLUMN_ENCODED_BYTES = 0;

CREATE SEQUENCE items_seq START 0 INCREMENT BY 1 CACHE 10;

CREATE INDEX items_idx ON items(info.id);