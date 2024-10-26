
DROP TABLE IF EXISTS CLAIM_TYPE;
DROP TABLE IF EXISTS DOCUMENT_TYPE;
DROP TABLE IF EXISTS ID_TYPE;
DROP TABLE IF EXISTS LAND_USE;

CREATE TABLE DOCUMENT_TYPE
(ID INT auto_increment PRIMARY KEY,
 CODE VARCHAR(255) NOT NULL,
 DISPLAY_VALUE VARCHAR(512) NOT NULL,
 ACTIVE BOOLEAN NOT NULL,
 DESCRIPTION VARCHAR(2048)); 
 
CREATE UNIQUE INDEX DOCUMENT_TYPE_IDX ON DOCUMENT_TYPE(CODE);

 CREATE TABLE ID_TYPE
(ID INT auto_increment PRIMARY KEY,
 TYPE VARCHAR(255) NOT NULL,
 DISPLAY_VALUE VARCHAR(512) NOT NULL,
 ACTIVE BOOLEAN NOT NULL,
 DESCRIPTION VARCHAR(2048)); 
 
CREATE UNIQUE INDEX ID_TYPE_IDX ON ID_TYPE(TYPE);

 CREATE TABLE LAND_USE
(ID INT auto_increment PRIMARY KEY,
 TYPE VARCHAR(255) NOT NULL,
 DISPLAY_VALUE VARCHAR(512) NOT NULL,
 ACTIVE BOOLEAN NOT NULL,
 DESCRIPTION VARCHAR(2048));
 
CREATE UNIQUE INDEX LAND_USE_IDX ON LAND_USE(TYPE);

CREATE TABLE CLAIM_TYPE
(ID INT auto_increment PRIMARY KEY,
 TYPE VARCHAR(255) NOT NULL,
 DISPLAY_VALUE VARCHAR(512) NOT NULL,
 ACTIVE BOOLEAN NOT NULL,
 DESCRIPTION VARCHAR(2048)); 
 
CREATE UNIQUE INDEX CLAIM_TYPE_IDX ON CLAIM_TYPE(TYPE); 


UPDATE CONFIGURATION SET "VALUE"='1.4.0' WHERE NAME='DBVERSION' AND "VALUE"='1.3.0';