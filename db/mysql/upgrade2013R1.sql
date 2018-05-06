-- DO NOT RUN THIS WITHOUT EXPERT KNOWLEDGE --

DROP TABLE IF EXISTS test_history;
DROP TABLE IF EXISTS test_case_files;
DROP TABLE IF EXISTS test_case_param;
DROP TABLE IF EXISTS test_case;
DROP TABLE IF EXISTS heartbeat;
DROP TABLE IF EXISTS trigger_release;
DROP TABLE IF EXISTS trigger_event;
DROP TABLE IF EXISTS trigger_;
DROP TABLE IF EXISTS script_execution;
DROP TABLE IF EXISTS unit_param_session;
DROP TABLE IF EXISTS syslog_event;

source tables/syslog_event.sql;
source tables/unit_param_session.sql
source tables/heartbeat.sql;
source tables/trigger.sql;
source tables/trigger_event.sql;
source tables/trigger_release.sql;
source tables/script_execution.sql;
source tables/test_case.sql;
source tables/test_case_param.sql;
source tables/test_case_files.sql;
source tables/test_history.sql;

-- update user (admin flag)
ALTER TABLE `xaps`.`user_` ADD COLUMN `is_admin` INT NOT NULL DEFAULT 0  AFTER `accesslist` ;
-- update syslog_event: NOT POSSIBLE, must be dropped and created - must therefore run a script to backup syslog-events
-- update filestore
ALTER TABLE `xaps`.`filestore` 
ADD 
	COLUMN `target_name` VARCHAR(128) NULL  AFTER `timestamp_` , ADD COLUMN `owner` INT(11) NULL  AFTER `target_name` , 
ADD 
  CONSTRAINT `fk_filestore_owner` FOREIGN KEY `fk_filestore_owner` (`owner`)
    REFERENCES `user_` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION;




