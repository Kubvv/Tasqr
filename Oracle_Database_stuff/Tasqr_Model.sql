/* Table drop */

/*DROP TABLE AClinker;
DROP TABLE APlinker;
DROP TABLE ATlinker;
DROP TABLE Account;
DROP TABLE Company;
DROP TABLE Project;
DROP TABLE Task;
DROP TABLE Subtask;*/

/* Main tables */
CREATE TABLE Account (
    userId NUMBER(6) PRIMARY KEY,
    name VARCHAR2(30),
    surname VARCHAR2(30),
	mail VARCHAR2(50),
	password VARCHAR2(50)
);

CREATE TABLE Company (
    companyId NUMBER(6) PRIMARY KEY,
    name VARCHAR2(30),
    description VARCHAR2(500)
);

CREATE TABLE Project (
    projectId NUMBER(8) PRIMARY KEY,
    name VARCHAR2(40),
    description VARCHAR2(500),
	companyId NUMBER(6) REFERENCES Company
);

CREATE TABLE Task (
    taskId NUMBER(10) PRIMARY KEY,
    name VARCHAR2(40),
    description VARCHAR2(200),
	projectId NUMBER(8) REFERENCES Project
);

CREATE TABLE Subtask (
    subtaskId NUMBER(12) PRIMARY KEY,
    name VARCHAR2(40),
    description VARCHAR2(100),
	taskId NUMBER(10) REFERENCES Task
);

/* Linekrs */

/* Account - Company linker */
CREATE TABLE AClinker (
    linkId NUMBER(10) PRIMARY KEY,
    userId NUMBER(6) REFERENCES Account,
    companyId NUMBER(6) REFERENCES Company
);

/* Account - Project linker */
CREATE TABLE APlinker (
    linkId NUMBER(10) PRIMARY KEY,
    userId NUMBER(6) REFERENCES Account,
    projectId NUMBER(8) REFERENCES Project
);

/* Account - Task linker */
CREATE TABLE ATlinker (
    linkId NUMBER(10) PRIMARY KEY,
    userId NUMBER(6) REFERENCES Account,
    taskId NUMBER(10) REFERENCES Task
);

/* Sequences for auto increment */
/*DROP SEQUENCE account_seq;
DROP SEQUENCE company_seq;
DROP SEQUENCE project_seq;
DROP SEQUENCE task_seq;
DROP SEQUENCE subtask_seq;
DROP SEQUENCE aclink_seq;
DROP SEQUENCE aplink_seq;
DROP SEQUENCE atlink_seq;*/

CREATE SEQUENCE account_seq START WITH 1;
CREATE SEQUENCE company_seq START WITH 1;
CREATE SEQUENCE project_seq START WITH 1;
CREATE SEQUENCE task_seq START WITH 1;
CREATE SEQUENCE subtask_seq START WITH 1;
CREATE SEQUENCE aclink_seq START WITH 1;
CREATE SEQUENCE aplink_seq START WITH 1;
CREATE SEQUENCE atlink_seq START WITH 1;

/* Auto increment triggers */

CREATE OR REPLACE TRIGGER autoIncAccount
BEFORE INSERT ON Account
FOR EACH ROW

BEGIN
    SELECT account_seq.nextval
    INTO :new.userId
    FROM dual;
END;
/

CREATE OR REPLACE TRIGGER autoIncCompany
BEFORE INSERT ON Company
FOR EACH ROW

BEGIN
    SELECT company_seq.nextval
    INTO :new.companyId
    FROM dual;
END;
/

CREATE OR REPLACE TRIGGER autoIncProject
BEFORE INSERT ON Project
FOR EACH ROW

BEGIN
    SELECT project_seq.nextval
    INTO :new.projectId
    FROM dual;
END;
/

CREATE OR REPLACE TRIGGER autoIncTask
BEFORE INSERT ON Task
FOR EACH ROW

BEGIN
    SELECT task_seq.nextval
    INTO :new.taskId
    FROM dual;
END;
/

CREATE OR REPLACE TRIGGER autoIncSubtask
BEFORE INSERT ON Subtask
FOR EACH ROW

BEGIN
    SELECT subtask_seq.nextval
    INTO :new.subtaskId
    FROM dual;
END;
/

CREATE OR REPLACE TRIGGER autoIncAClink
BEFORE INSERT ON AClinker
FOR EACH ROW

BEGIN
    SELECT aclink_seq.nextval
    INTO :new.linkId
    FROM dual;
END;
/

CREATE OR REPLACE TRIGGER autoIncAPlink
BEFORE INSERT ON APlinker
FOR EACH ROW

BEGIN
    SELECT aplink_seq.nextval
    INTO :new.linkId
    FROM dual;
END;
/

CREATE OR REPLACE TRIGGER autoIncATlink
BEFORE INSERT ON ATlinker
FOR EACH ROW

BEGIN
    SELECT atlink_seq.nextval
    INTO :new.linkId
    FROM dual;
END;
/