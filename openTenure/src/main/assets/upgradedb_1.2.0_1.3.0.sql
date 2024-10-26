MERGE INTO CONFIGURATION(CONFIGURATION_ID, NAME, "VALUE") KEY (CONFIGURATION_ID) SELECT '2', 'PROTOVERSION', '1.0' FROM DUAL;
MERGE INTO LINK(LINK_ID, URL, DESC) KEY (LINK_ID) SELECT '1', 'https://demo.opentenure.org', 'OpenTenure Community: visit the OpenTenure Community web site and tell us what you think.' FROM DUAL;

CREATE TABLE MAP_BOOKMARK
(MAP_BOOKMARK_ID VARCHAR(255) PRIMARY KEY, 
NAME VARCHAR(255) NOT NULL, 
LAT DECIMAL(15,10) NOT NULL, 
LON DECIMAL(15,10) NOT NULL);

CREATE UNIQUE INDEX MAP_BOOKMARK_NAME_IDX ON MAP_BOOKMARK(NAME);

UPDATE CONFIGURATION SET "VALUE"='1.3.0' WHERE NAME='DBVERSION' AND "VALUE"='1.2.0';