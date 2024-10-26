DELETE FROM LINK;

MERGE INTO LINK(LINK_ID, URL, DESC) KEY (LINK_ID) SELECT '1', 'https://www.fao.org/tenure/sola-suite/open-tenure/en', 'OpenTenure: visit the OpenTenure page and tell us what you think.' FROM DUAL;
MERGE INTO LINK(LINK_ID, URL, DESC) KEY (LINK_ID) SELECT '2', 'https://www.fao.org/tenure/sola-suite/sola-suite/en/', 'FLOSS SOLA: look at the SOLA Suite page to discover other products of SOLA family.' FROM DUAL;
MERGE INTO LINK(LINK_ID, URL, DESC) KEY (LINK_ID) SELECT '3', 'https://github.com/OpenTenure/ot-android-studio/blob/master/PRIVACY.md', 'Have a look at the privacy policy before using Open Tenure' FROM DUAL;

UPDATE CONFIGURATION SET "VALUE"='2.1.0' WHERE NAME='DBVERSION' AND "VALUE"='2.0.0';

