SET SQL_MODE = 'ALLOW_INVALID_DATES';

DROP TABLE IF EXISTS `unit_type`;
CREATE TABLE `unit_type`
(
  `unit_type_id`   INTEGER     NOT NULL AUTO_INCREMENT,
  `matcher_id`     VARCHAR(32) NULL,
  `unit_type_name` VARCHAR(64) NOT NULL,
  `vendor_name`    VARCHAR(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  `description`    VARCHAR(2000) CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  `protocol`       VARCHAR(16) NOT NULL,
  PRIMARY KEY (`unit_type_id`),
  UNIQUE INDEX `uq_unit_type_name` (`unit_type_name`(64))
);

DROP TABLE IF EXISTS `unit_type_param`;
CREATE TABLE `unit_type_param`
(
  `unit_type_param_id` INTEGER      NOT NULL AUTO_INCREMENT,
  `unit_type_id`       INTEGER      NOT NULL,
  `name`               VARCHAR(255) NOT NULL,
  `flags`              VARCHAR(32)  NOT NULL,
  PRIMARY KEY (`unit_type_param_id`),
  UNIQUE INDEX `idx_u_t_p_unit_type_id_name` (`unit_type_id`, `name`(255)),
  CONSTRAINT `fk_u_t_p_unit_type_id` FOREIGN KEY `fk_u_t_p_unit_type_id` (`unit_type_id`)
    REFERENCES `unit_type` (`unit_type_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
);


DROP TABLE IF EXISTS `unit_type_param_value`;
CREATE TABLE `unit_type_param_value`
(
  `unit_type_param_id` INTEGER                                                 NOT NULL,
  `value`              VARCHAR(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `priority`           INTEGER                                                 NOT NULL,
  `type`               VARCHAR(32)                                             NOT NULL DEFAULT 'enum',
  PRIMARY KEY (`unit_type_param_id`, `value`),
  CONSTRAINT `fk_unit_param_value_utpid` FOREIGN KEY `fk_unit_param_value_utpid` (`unit_type_param_id`)
    REFERENCES `unit_type_param` (`unit_type_param_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
);

DROP TABLE IF EXISTS `profile`;
CREATE TABLE `profile`
(
  `profile_id`   INTEGER     NOT NULL AUTO_INCREMENT,
  `unit_type_id` INTEGER     NOT NULL,
  `profile_name` VARCHAR(64) NOT NULL,
  PRIMARY KEY (`profile_id`),
  UNIQUE INDEX `idx_unit_type_id_profile_name` (`unit_type_id`, `profile_name`(64)),
  CONSTRAINT `fk_profile_unit_type_id` FOREIGN KEY `fk_profile_unit_type_id` (`unit_type_id`)
    REFERENCES `unit_type` (`unit_type_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
);

DROP TABLE IF EXISTS `profile_param`;
CREATE TABLE `profile_param`
(
  `profile_id`         INTEGER NOT NULL,
  `unit_type_param_id` INTEGER NOT NULL,
  `value`              VARCHAR(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  PRIMARY KEY (`profile_id`, `unit_type_param_id`),
  CONSTRAINT `fk_profile_param_profile_id` FOREIGN KEY `fk_profile_param_profile_id` (`profile_id`)
    REFERENCES `profile` (`profile_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_profile_param_u_t_p_id` FOREIGN KEY `fk_profile_param_u_t_p_id` (`unit_type_param_id`)
    REFERENCES `unit_type_param` (`unit_type_param_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
);

DROP TABLE IF EXISTS `unit`;
CREATE TABLE `unit`
(
  `unit_id`      VARCHAR(64) NOT NULL,
  `unit_type_id` INTEGER     NOT NULL,
  `profile_id`   INTEGER     NOT NULL,
  PRIMARY KEY (`unit_id`),
  INDEX          `idx_unit_unit_type_profile` (`unit_type_id`, `profile_id`, `unit_id`),
  INDEX          `idx_unit_profile_unit_type` (`profile_id`, `unit_type_id`, `unit_id`),
  CONSTRAINT `fk_unit_unit_type_id` FOREIGN KEY `fk_unit_unit_type_id` (`unit_type_id`)
    REFERENCES `unit_type` (`unit_type_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_unit_profile_id` FOREIGN KEY `fk_unit_profile_id` (`profile_id`)
    REFERENCES `profile` (`profile_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
);

DROP TABLE IF EXISTS `unit_param`;
CREATE TABLE `unit_param`
(
  `unit_id`            VARCHAR(64) NOT NULL,
  `unit_type_param_id` INTEGER     NOT NULL,
  `value`              VARCHAR(512) CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  PRIMARY KEY (`unit_id`, `unit_type_param_id`),
  INDEX                `idx_unit_param_type_id2` (`unit_type_param_id`, `value`),
  INDEX                `idx_unit_param_value` (`value`),
  CONSTRAINT `fk_unit_param_unit_id` FOREIGN KEY `fk_unit_param_unit_id` (`unit_id`)
    REFERENCES `unit` (`unit_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_unit_param_u_t_p_id` FOREIGN KEY `fk_unit_param_u_t_p_id` (`unit_type_param_id`)
    REFERENCES `unit_type_param` (`unit_type_param_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
);

DROP TABLE IF EXISTS `unit_param_session`;
CREATE TABLE `unit_param_session`
(
  `unit_id`            VARCHAR(64) NOT NULL,
  `unit_type_param_id` INTEGER     NOT NULL,
  `value`              VARCHAR(512) CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  PRIMARY KEY (`unit_id`, `unit_type_param_id`),
  CONSTRAINT `fk_unit_param_session_unit_id` FOREIGN KEY `fk_unit_param_session_unit_id` (`unit_id`)
    REFERENCES `unit` (`unit_id`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_unit_param_session_u_t_p_id` FOREIGN KEY `fk_unit_param_session_u_t_p_id` (`unit_type_param_id`)
    REFERENCES `unit_type_param` (`unit_type_param_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
);

DROP TABLE IF EXISTS `group_`;
CREATE TABLE `group_`
(
  `group_id`          INTEGER     NOT NULL AUTO_INCREMENT,
  `unit_type_id`      INTEGER     NOT NULL,
  `group_name`        VARCHAR(64) NOT NULL,
  `description`       VARCHAR(2000) CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  `parent_group_id`   INTEGER NULL,
  `profile_id`        INTEGER NULL,
  `count`             INTEGER NULL,
  `time_param_id`     INTEGER NULL,
  `time_rolling_rule` VARCHAR(32) NULL,
  PRIMARY KEY (`group_id`),
  CONSTRAINT `fk_group__unit_type_id` FOREIGN KEY `fk_group__unit_type_id` (`unit_type_id`)
    REFERENCES `unit_type` (`unit_type_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_group__group_id` FOREIGN KEY `fk_group__group_id` (`parent_group_id`)
    REFERENCES `group_` (`group_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_group__profile_id` FOREIGN KEY `fk_group__profile_id` (`profile_id`)
    REFERENCES `profile` (`profile_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_time_param_u_t_p_id` FOREIGN KEY `fk_time_param_u_t_p_id` (`time_param_id`)
    REFERENCES `unit_type_param` (`unit_type_param_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
);

DROP TABLE IF EXISTS `group_param`;
CREATE TABLE `group_param`
(
  `id`                 INTEGER     NOT NULL AUTO_INCREMENT,
  `group_id`           INTEGER     NOT NULL,
  `unit_type_param_id` INTEGER     NOT NULL,
  `operator`           VARCHAR(2)  NOT NULL DEFAULT '=',
  `data_type`          VARCHAR(32) NOT NULL DEFAULT 'TEXT',
  `value`              VARCHAR(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_group_param_group_id` FOREIGN KEY `fk_group_param_group_id` (`group_id`)
    REFERENCES `group_` (`group_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_group_param_u_t_p_id` FOREIGN KEY `fk_group_param_u_t_p_id` (`unit_type_param_id`)
    REFERENCES `unit_type_param` (`unit_type_param_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
);

DROP TABLE IF EXISTS `user_`;
CREATE TABLE `user_`
(
  `id`         INTEGER                                                NOT NULL AUTO_INCREMENT,
  `username`   VARCHAR(64)                                            NOT NULL,
  `secret`     VARCHAR(64)                                            NOT NULL,
  `fullname`   VARCHAR(64) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `accesslist` VARCHAR(256)                                           NOT NULL,
  `is_admin`   INTEGER                                                NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `idx_username` (`username`(64))
);
DROP TABLE IF EXISTS `permission_`;
CREATE TABLE `permission_`
(
  `id`           INTEGER NOT NULL AUTO_INCREMENT,
  `user_id`      INTEGER NOT NULL,
  `unit_type_id` INTEGER NOT NULL,
  `profile_id`   INTEGER NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `idx_uid_utpid_pid` (`user_id`,`unit_type_id`,`profile_id`),
  CONSTRAINT `fk_permission_user_id` FOREIGN KEY
    `fk_permission_user_id`
    (`user_id`)
    REFERENCES `user_` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_permission_unit_type_id` FOREIGN KEY
    `fk_permission_unit_type_id`
    (`unit_type_id`)
    REFERENCES `unit_type` (`unit_type_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
);

DROP TABLE IF EXISTS `filestore`;
CREATE TABLE `filestore`
(
  `id`           INTEGER     NOT NULL AUTO_INCREMENT,
  `name`         VARCHAR(64) NOT NULL,
  `unit_type_id` INTEGER     NOT NULL,
  `type`         VARCHAR(64) NOT NULL DEFAULT 'SOFTWARE',
  `description`  VARCHAR(2000) CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  `version`      VARCHAR(64) NOT NULL,
  `content`      LONGBLOB    NOT NULL,
  `timestamp_`   DATETIME    NOT NULL,
  `target_name`  VARCHAR(128) NULL,
  `owner`        INTEGER NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `idx_utpid_t_st_v` (`unit_type_id`, `type`(64), `version`(64)),
  UNIQUE INDEX `idx_filestore_utpid_name` (`unit_type_id`, `name`(64)),
  CONSTRAINT `fk_filestore_unit_type_id` FOREIGN KEY `fk_filestore_unit_type_id` (`unit_type_id`)
    REFERENCES `unit_type` (`unit_type_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_filestore_owner` FOREIGN KEY `fk_filestore_owner` (`owner`)
    REFERENCES `user_` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
);

DROP TABLE IF EXISTS `syslog_event`;
CREATE TABLE `syslog_event`
(
  `id`                INTEGER     NOT NULL AUTO_INCREMENT,
  `syslog_event_id`   INTEGER     NOT NULL,
  `syslog_event_name` VARCHAR(64) NOT NULL,
  `unit_type_id`      INTEGER     NOT NULL,
  `group_id`          INTEGER NULL,
  `expression`        VARCHAR(64) NOT NULL DEFAULT 'Specify an expression',
  `store_policy`      VARCHAR(16) NOT NULL DEFAULT 'STORE',
  `filestore_id`      INTEGER,
  `description`       VARCHAR(1024) CHARACTER SET utf8 COLLATE utf8_general_ci,
  `delete_limit`      INTEGER,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_syslogevent__unit_type_id` FOREIGN KEY `fk_syslogevent__unit_type_id` (`unit_type_id`)
    REFERENCES `unit_type` (`unit_type_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_syslogevent__group_id` FOREIGN KEY `fk_syslogevent__group_id` (`group_id`)
    REFERENCES `group_` (`group_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_syslogevent_filestore_id` FOREIGN KEY `fk_syslogevent_filestore_id` (`filestore_id`)
    REFERENCES `filestore` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  UNIQUE INDEX `idx_syslog_event_id_unit_type_name` (`syslog_event_id`, `unit_type_id`)
);

DROP TABLE IF EXISTS `job`;
CREATE TABLE `job`
(
  `job_id`                INTEGER     NOT NULL AUTO_INCREMENT,
  `job_name`              VARCHAR(64) NOT NULL,
  `job_type`              VARCHAR(32) NOT NULL,
  `description`           VARCHAR(2000) CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  `group_id`              INTEGER     NOT NULL,
  `unconfirmed_timeout`   INTEGER     NOT NULL,
  `stop_rules`            VARCHAR(255) NULL,
  `status`                VARCHAR(32) NOT NULL,
  `completed_no_failure`  INTEGER     NOT NULL,
  `completed_had_failure` INTEGER     NOT NULL,
  `confirmed_failed`      INTEGER     NOT NULL,
  `unconfirmed_failed`    INTEGER     NOT NULL,
  `start_timestamp`       DATETIME NULL,
  `end_timestamp`         DATETIME NULL,
  `firmware_id`           INTEGER NULL,
  `job_id_dependency`     INTEGER NULL,
  `profile_id`            INTEGER NULL,
  `repeat_count`          INTEGER NULL,
  `repeat_interval`       INTEGER NULL,
  PRIMARY KEY (`job_id`),
  CONSTRAINT `fk_job_group_id` FOREIGN KEY `fk_job_group_id` (`group_id`)
    REFERENCES `group_` (`group_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_job_firmware_id` FOREIGN KEY `fk_job_filestore_id` (`firmware_id`)
    REFERENCES `filestore` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_job_job_id` FOREIGN KEY `fk_job_job_id` (`job_id_dependency`)
    REFERENCES `job` (`job_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_job_profile_id` FOREIGN KEY `fk_job_profile_id` (`profile_id`)
    REFERENCES `profile` (`profile_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION

);

DROP TABLE IF EXISTS `job_param`;
CREATE TABLE `job_param`
(
  `job_id`             INTEGER     NOT NULL,
  `unit_id`            VARCHAR(64) NOT NULL,
  `unit_type_param_id` INTEGER     NOT NULL,
  `value`              VARCHAR(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  PRIMARY KEY (`job_id`, `unit_id`, `unit_type_param_id`),
  CONSTRAINT `fk_job_param_job_id` FOREIGN KEY `fk_job_param_job_id` (`job_id`)
    REFERENCES `job` (`job_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_job_param_u_t_p_id` FOREIGN KEY `fk_job_param_u_t_p_id` (`unit_type_param_id`)
    REFERENCES `unit_type_param` (`unit_type_param_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
);

DROP TABLE IF EXISTS `unit_job`;
CREATE TABLE `unit_job`
(
  `unit_id`         VARCHAR(64) NOT NULL,
  `job_id`          INTEGER     NOT NULL,
  `start_timestamp` DATETIME    NOT NULL,
  `end_timestamp`   DATETIME NULL,
  `status`          VARCHAR(32) NOT NULL,
  `processed`       INTEGER NULL DEFAULT '0',
  `confirmed`       INTEGER NULL DEFAULT '0',
  `unconfirmed`     INTEGER NULL DEFAULT '0',
  PRIMARY KEY (`unit_id`, `job_id`),
  CONSTRAINT `fk_unit_job_unit_id` FOREIGN KEY `fk_unit_job_unit_id` (`unit_id`)
    REFERENCES `unit` (`unit_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_unit_job_job_id` FOREIGN KEY `fk_unit_job_job_id` (`job_id`)
    REFERENCES `job` (`job_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  INDEX             `idx_unit_job_1` (`status`(32), `start_timestamp`),
  INDEX             `idx_unit_job_2` (`processed`)
);

DROP TABLE IF EXISTS `heartbeat`;
CREATE TABLE `heartbeat`
(
  `id`                     INTEGER     NOT NULL AUTO_INCREMENT,
  `name`                   VARCHAR(64) NOT NULL,
  `unit_type_id`           INTEGER     NOT NULL,
  `heartbeat_expression`   VARCHAR(64) NOT NULL,
  `heartbeat_group_id`     INTEGER     NOT NULL,
  `heartbeat_timeout_hour` INTEGER     NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_hb_group_id` FOREIGN KEY `fk_hb_group_id` (`heartbeat_group_id`)
    REFERENCES `group_` (`group_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_hb_unit_type_id` FOREIGN KEY `fk_hb_unit_type_id` (`unit_type_id`)
    REFERENCES `unit_type` (`unit_type_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
);

DROP TABLE IF EXISTS `trigger_`;
CREATE TABLE `trigger_`
(
  `id`                    INTEGER      NOT NULL AUTO_INCREMENT,
  `name`                  VARCHAR(255) NOT NULL,
  `description`           VARCHAR(1024),
  -- BASIC or COMPOSITE (0 or 1)
  `trigger_type`          INTEGER      NOT NULL DEFAULT 0,
  -- ALARM, REPORT or SILENT (0-2)
  `notify_type`           INTEGER      NOT NULL DEFAULT 0,
  -- 0 or 1
  `active`                INTEGER      NOT NULL DEFAULT 0,
  `unit_type_id`          INTEGER      NOT NULL,
  `group_id`              INTEGER,
  -- 15 to 120 (minutes)
  `eval_period_minutes`   INTEGER      NOT NULL,
  -- 1 to 168 (hours)
  `notify_interval_hours` INTEGER NULL,
  -- REFERS TO FILESTORE.ID
  `filestore_id`          INTEGER,
  -- REFERS TO ID
  `parent_trigger_id`     INTEGER,
  `to_list`               VARCHAR(512),
  -- JUST FOR BASIC
  `syslog_event_id`       INTEGER,
  `no_events`             INTEGER,
  `no_events_pr_unit`     INTEGER,
  `no_units`              INTEGER,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `idx_trigger_unit_type_id_name` (`unit_type_id`, `name`(255)),
  CONSTRAINT `fk_trigger_unit_type_id` FOREIGN KEY `fk_trigger_unit_type_id` (`unit_type_id`)
    REFERENCES `unit_type` (`unit_type_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_trigger_group_id` FOREIGN KEY `fk_trigger_group_id` (`group_id`)
    REFERENCES `group_` (`group_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_trigger_filestore_id` FOREIGN KEY `fk_trigger_filestore_id` (`filestore_id`)
    REFERENCES `filestore` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_trigger_parent_id` FOREIGN KEY `fk_trigger_parent_id` (`parent_trigger_id`)
    REFERENCES `trigger_` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_trigger_syslog_event_id` FOREIGN KEY `fk_trigger_syslog_event_id` (`syslog_event_id`)
    REFERENCES `syslog_event` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
);

DROP TABLE IF EXISTS `trigger_event`;
CREATE TABLE `trigger_event`
(
  `id`         INTEGER     NOT NULL AUTO_INCREMENT,
  `timestamp_` DATETIME    NOT NULL,
  `trigger_id` INTEGER     NOT NULL,
  `unit_id`    VARCHAR(64) NOT NULL, -- We skip foreign key referenec on unit -> increase performance
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_trigger_event_trigger_id` FOREIGN KEY `fk_trigger_event_trigger_id` (`trigger_id`)
    REFERENCES `trigger_` (`id`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
);

DROP TABLE IF EXISTS `trigger_release`;
CREATE TABLE `trigger_release`
(
  `id`                    INTEGER  NOT NULL AUTO_INCREMENT,
  `trigger_id`            INTEGER  NOT NULL,
  `no_events`             INTEGER NULL,
  `no_events_pr_unit`     INTEGER NULL,
  `no_units`              INTEGER NULL,
  `first_event_timestamp` DATETIME NOT NULL,
  `release_timestamp`     DATETIME NOT NULL,
  `sent_timestamp`        DATETIME,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_trigger_release_trigger_id` FOREIGN KEY `fk_trigger_release_trigger_id` (`trigger_id`)
    REFERENCES `trigger_` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
);

-- Tables with no or few foreign keys
DROP TABLE IF EXISTS `certificate`;
CREATE TABLE `certificate`
(
  `id`          INTEGER      NOT NULL AUTO_INCREMENT,
  `name`        VARCHAR(64)  NOT NULL,
  `certificate` VARCHAR(256) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `idx_name` (`name`(64))
);

DROP TABLE IF EXISTS `message`;
CREATE TABLE `message`
(
  `id`          INTEGER     NOT NULL AUTO_INCREMENT,
  `type`        VARCHAR(64) NOT NULL,
  `sender`      VARCHAR(64) NOT NULL,
  `receiver`    VARCHAR(64),
  `object_type` VARCHAR(64),
  `object_id`   VARCHAR(64),
  `timestamp_`  DATETIME    NOT NULL,
  `content`     VARCHAR(2000) CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  PRIMARY KEY (`id`)
);

DROP TABLE IF EXISTS `monitor_event`;
CREATE TABLE `monitor_event`
(
  `event_id`     BIGINT      NOT NULL AUTO_INCREMENT,
  `module_name`  VARCHAR(32) NOT NULL,
  `module_key`   VARCHAR(32) NOT NULL,
  `module_state` INTEGER     NOT NULL,
  `message`      VARCHAR(2000) CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  `starttime`    TIMESTAMP   NOT NULL,
  `endtime`      TIMESTAMP   NOT NULL,
  `lastchecked`  TIMESTAMP   NOT NULL,
  `url`          VARCHAR(255),
  PRIMARY KEY (`event_id`),
  CONSTRAINT NameAndKey UNIQUE (module_name, module_key)
);

DROP TABLE IF EXISTS `script_execution`;
CREATE TABLE `script_execution`
(
  `id`                INTEGER  NOT NULL AUTO_INCREMENT,
  `unit_type_id`      INTEGER  NOT NULL, -- SET BY REQUEST-CLIENT
  `filestore_id`      INTEGER  NOT NULL, -- SET BY REQUEST-CLIENT
  `arguments`         VARCHAR(1024), -- SET BY REQUEST-CLIENT
  `request_timestamp` DATETIME NOT NULL, -- SET BY REQUEST-CLIENT
  `request_id`        VARCHAR(32), -- SET BY REQUEST-CLIENT
  `start_timestamp`   DATETIME, -- SET BY SSD
  `end_timestamp`     DATETIME, -- SET BY SSD
  `exit_status`       INTEGER, -- SET BY SSD (0=SUCCESS, 1=ERROR)
  `error_message`     VARCHAR(1024), -- SET BY SSD IF NECESSARY
  PRIMARY KEY (`id`)
);

DROP TABLE IF EXISTS `syslog`;
CREATE TABLE `syslog`
(
  `syslog_id`           BIGINT                                                   NOT NULL AUTO_INCREMENT,
  `collector_timestamp` DATETIME                                                 NOT NULL,
  `syslog_event_id`     INTEGER                                                  NOT NULL,
  `facility`            INTEGER                                                  NOT NULL,
  `facility_version`    VARCHAR(48) NULL,
  `severity`            INTEGER                                                  NOT NULL,
  `device_timestamp`    VARCHAR(32) NULL,
  `hostname`            VARCHAR(32) NULL,
  `tag`                 VARCHAR(32) NULL,
  `content`             VARCHAR(1024) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `flags`               VARCHAR(32) NULL,
  `ipaddress`           VARCHAR(32) NULL,
  `unit_id`             VARCHAR(64) NULL,
  `profile_name`        VARCHAR(64) NULL,
  `unit_type_name`      VARCHAR(64) NULL,
  `user_id`             VARCHAR(32) NULL,
  PRIMARY KEY (`syslog_id`),
  INDEX                 `idx_syslog_coll_tms` (`collector_timestamp` ASC, `severity` ASC, `syslog_event_id` ASC),
  INDEX                 `idx_syslog_unit_id_coll_tms` (`unit_id` ASC, `collector_timestamp` ASC)
);

DROP TABLE IF EXISTS `report_unit`;
CREATE TABLE `report_unit`
(
  `timestamp_`       DATETIME    NOT NULL,
  `period_type`      INTEGER     NOT NULL,
  `unit_type_name`   VARCHAR(64) NOT NULL,
  `profile_name`     VARCHAR(64) NOT NULL,
  `software_version` VARCHAR(64) NOT NULL,
  `status`           VARCHAR(32) NOT NULL,
  `unit_count`       INTEGER     NOT NULL,
  PRIMARY KEY (`timestamp_`, `period_type`, `unit_type_name`, `profile_name`, `software_version`, `status`)
);

DROP TABLE IF EXISTS `report_group`;
CREATE TABLE `report_group`
(
  `timestamp_`     DATETIME    NOT NULL,
  `period_type`    INTEGER     NOT NULL,
  `unit_type_name` VARCHAR(64) NOT NULL,
  `group_name`     VARCHAR(64) NOT NULL,
  `unit_count`     INTEGER     NOT NULL,
  PRIMARY KEY (`timestamp_`, `period_type`, `unit_type_name`, `group_name`)
);

DROP TABLE IF EXISTS `report_job`;
CREATE TABLE `report_job`
(
  `timestamp_`         DATETIME    NOT NULL,
  `period_type`        INTEGER     NOT NULL,
  `unit_type_name`     VARCHAR(64) NOT NULL,
  `job_name`           VARCHAR(64) NOT NULL,
  `group_name`         VARCHAR(64) NOT NULL,
  `group_size`         INTEGER     NOT NULL,
  `completed`          INTEGER     NOT NULL,
  `confirmed_failed`   INTEGER     NOT NULL,
  `unconfirmed_failed` INTEGER     NOT NULL,
  PRIMARY KEY (`timestamp_`, `period_type`, `unit_type_name`, `job_name`)
);

DROP TABLE IF EXISTS `report_syslog`;
CREATE TABLE `report_syslog`
(
  `timestamp_`      DATETIME    NOT NULL,
  `period_type`     INTEGER     NOT NULL,
  `unit_type_name`  VARCHAR(64) NOT NULL,
  `profile_name`    VARCHAR(64) NOT NULL,
  `severity`        VARCHAR(16) NOT NULL,
  `syslog_event_id` INTEGER     NOT NULL,
  `facility`        VARCHAR(32) NOT NULL,
  `unit_count`      INTEGER     NOT NULL,
  PRIMARY KEY (`timestamp_`, `period_type`, `unit_type_name`, `profile_name`, `severity`, `syslog_event_id`, `facility`)
);

DROP TABLE IF EXISTS `report_prov`;
CREATE TABLE `report_prov`
(
  `timestamp_`         DATETIME    NOT NULL,
  `period_type`        INTEGER     NOT NULL,
  `unit_type_name`     VARCHAR(64) NOT NULL,
  `profile_name`       VARCHAR(64) NOT NULL,
  `software_version`   VARCHAR(64) NOT NULL,
  `prov_output`        VARCHAR(16) NOT NULL,
  `ok_count`           INTEGER,
  `rescheduled_count`  INTEGER,
  `error_count`        INTEGER,
  `missing_count`      INTEGER,
  `session_length_avg` INTEGER,
  PRIMARY KEY (`timestamp_`, `period_type`, `unit_type_name`, `profile_name`, `software_version`, `prov_output`)
);

DROP TABLE IF EXISTS `report_voip`;
CREATE TABLE `report_voip`
(
  `timestamp_`                 DATETIME    NOT NULL,
  `period_type`                INTEGER     NOT NULL,
  `unit_type_name`             VARCHAR(64) NOT NULL,
  `profile_name`               VARCHAR(64) NOT NULL,
  `software_version`           VARCHAR(64) NOT NULL,
  `line`                       INTEGER     NOT NULL,
  `mos_avg`                    INTEGER,
  `jitter_avg`                 INTEGER,
  `jitter_max`                 INTEGER,
  `percent_loss_avg`           INTEGER,
  `call_length_avg`            INTEGER,
  `call_length_total`          INTEGER     NOT NULL,
  `incoming_call_count`        INTEGER     NOT NULL,
  `outgoing_call_count`        INTEGER     NOT NULL,
  `outgoing_call_failed_count` INTEGER     NOT NULL,
  `aborted_call_count`         INTEGER     NOT NULL,
  `no_sip_service_time`        INTEGER     NOT NULL,
  PRIMARY KEY (`timestamp_`, `period_type`, `unit_type_name`, `profile_name`, `software_version`, `line`)
);

DROP TABLE IF EXISTS `report_voip_tr`;
CREATE TABLE `report_voip_tr`
(
  `timestamp_`                 DATETIME    NOT NULL,
  `period_type`                INTEGER     NOT NULL,
  `unit_type_name`             VARCHAR(64) NOT NULL,
  `profile_name`               VARCHAR(64) NOT NULL,
  `software_version`           VARCHAR(64) NOT NULL,
  `line`                       VARCHAR(16) NOT NULL,
  `line_status`                VARCHAR(64) NOT NULL,
  `overruns_count`             INTEGER     NOT NULL,
  `underruns_count`            INTEGER     NOT NULL,
  `percent_loss_avg`           INTEGER,
  `call_length_avg`            INTEGER,
  `call_length_total`          INTEGER     NOT NULL,
  `incoming_call_count`        INTEGER     NOT NULL,
  `outgoing_call_count`        INTEGER     NOT NULL,
  `outgoing_call_failed_count` INTEGER     NOT NULL,
  `aborted_call_count`         INTEGER     NOT NULL,
  `no_sip_service_time`        INTEGER     NOT NULL,
  PRIMARY KEY (`timestamp_`, `period_type`, `unit_type_name`, `profile_name`, `software_version`, `line`, `line_status`)
);

DROP TABLE IF EXISTS `report_hw`;
CREATE TABLE `report_hw`
(
  `timestamp_`               DATETIME    NOT NULL,
  `period_type`              INTEGER     NOT NULL,
  `unit_type_name`           VARCHAR(64) NOT NULL,
  `profile_name`             VARCHAR(64) NOT NULL,
  `software_version`         VARCHAR(64) NOT NULL,
  `boot_count`               INTEGER     NOT NULL,
  `boot_watchdog_count`      INTEGER     NOT NULL,
  `boot_misc_count`          INTEGER     NOT NULL,
  `boot_power_count`         INTEGER     NOT NULL,
  `boot_reset_count`         INTEGER     NOT NULL,
  `boot_prov_count`          INTEGER     NOT NULL,
  `boot_prov_sw_count`       INTEGER     NOT NULL,
  `boot_prov_conf_count`     INTEGER     NOT NULL,
  `boot_prov_boot_count`     INTEGER     NOT NULL,
  `boot_user_count`          INTEGER     NOT NULL,
  `mem_heap_ddr_pool_avg`    INTEGER,
  `mem_heap_ddr_current_avg` INTEGER,
  `mem_heap_ddr_low_avg`     INTEGER,
  `mem_heap_ocm_pool_avg`    INTEGER,
  `mem_heap_ocm_current_avg` INTEGER,
  `mem_heap_ocm_low_avg`     INTEGER,
  `mem_np_ddr_pool_avg`      INTEGER,
  `mem_np_ddr_current_avg`   INTEGER,
  `mem_np_ddr_low_avg`       INTEGER,
  `mem_np_ocm_pool_avg`      INTEGER,
  `mem_np_ocm_current_avg`   INTEGER,
  `mem_np_ocm_low_avg`       INTEGER,
  `cpe_uptime_avg`           INTEGER,
  PRIMARY KEY (`timestamp_`, `period_type`, `unit_type_name`, `profile_name`, `software_version`)
);

DROP TABLE IF EXISTS `report_hw_tr`;
CREATE TABLE `report_hw_tr`
(
  `timestamp_`          DATETIME    NOT NULL,
  `period_type`         INTEGER     NOT NULL,
  `unit_type_name`      VARCHAR(64) NOT NULL,
  `profile_name`        VARCHAR(64) NOT NULL,
  `software_version`    VARCHAR(64) NOT NULL,
  `cpe_uptime_avg`      INTEGER,
  `memory_total_avg`    INTEGER,
  `memory_free_avg`     INTEGER,
  `cpu_usage_avg`       INTEGER,
  `process_count_avg`   INTEGER,
  `temperature_now_avg` INTEGER,
  `temperature_max_avg` INTEGER,
  PRIMARY KEY (`timestamp_`, `period_type`, `unit_type_name`, `profile_name`, `software_version`)
);

DROP TABLE IF EXISTS `report_gateway_tr`;
CREATE TABLE `report_gateway_tr`
(
  `timestamp_`             DATETIME    NOT NULL,
  `period_type`            INTEGER     NOT NULL,
  `unit_type_name`         VARCHAR(64) NOT NULL,
  `profile_name`           VARCHAR(64) NOT NULL,
  `software_version`       VARCHAR(64) NOT NULL,
  `ping_success_count_avg` INTEGER,
  `ping_failure_count_avg` INTEGER,
  `ping_response_time_avg` INTEGER,
  `download_speed_avg`     INTEGER,
  `upload_speed_avg`       INTEGER,
  `wan_uptime_avg`         INTEGER,
  PRIMARY KEY (`timestamp_`, `period_type`, `unit_type_name`, `profile_name`, `software_version`)
);

-- Setup initial admin user with default password "freeacs"
INSERT INTO user_ (id, username, secret, fullname, accesslist, is_admin)
VALUES (1, 'admin', '4E9BA006A68A8767D65B3761E038CF9040C54A00', 'Admin user', 'Admin', 1);
