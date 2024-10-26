DROP TABLE IF EXISTS TASK;

CREATE TABLE TASK
(TASK_ID VARCHAR(255) PRIMARY KEY,
STARTED TIMESTAMP NOT NULL,
COMPLETION DECIMAL(5,2) NOT NULL);

UPDATE CONFIGURATION SET "VALUE"='1.2.0' WHERE NAME='DBVERSION' AND "VALUE"='1.1.0';