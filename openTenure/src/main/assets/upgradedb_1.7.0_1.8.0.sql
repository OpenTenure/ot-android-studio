ALTER TABLE CLAIM ADD COLUMN IF NOT EXISTS DELETED BOOLEAN NOT NULL DEFAULT FALSE;

UPDATE CONFIGURATION SET "VALUE"='1.8.0' WHERE NAME='DBVERSION' AND "VALUE"='1.7.0';
UPDATE CONFIGURATION SET "VALUE"='1.1' WHERE NAME='PROTOVERSION' AND "VALUE"='1.0';
