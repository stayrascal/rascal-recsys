CREATE TABLE recommendation(
userId BIGINT NOT NULL,
itemId BIGINT NOT NULL,
score FLOAT,
measureType VARCHAR NOT NULL
CONSTRAINT pk PRIMARY KEY (userId, itemId, measureType)
) SALT_BUCKETS = 3;