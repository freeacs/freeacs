DROP TABLE IF EXISTS `syslog`;
CREATE TABLE `syslog` (
  `syslog_id` BIGINT NOT NULL AUTO_INCREMENT,
  `collector_timestamp` DATETIME NOT NULL,
  `syslog_event_id` INTEGER NOT NULL,
  `facility` INTEGER NOT NULL,
  `facility_version` VARCHAR(48) NULL,
  `severity` INTEGER NOT NULL,
  `device_timestamp` VARCHAR(32) NULL,
  `hostname` VARCHAR(32) NULL,
  `tag` VARCHAR(32) NULL,
  `content` VARCHAR(1024) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `flags` VARCHAR(32) NULL,
  `ipaddress` VARCHAR(32) NULL,
  `unit_id` VARCHAR(64) NULL,
  `profile_name` VARCHAR(64) NULL,
  `unit_type_name` VARCHAR(64) NULL,
  `user_id` VARCHAR(32) NULL,
  PRIMARY KEY (`syslog_id`),
  INDEX `idx_syslog_coll_tms` (`collector_timestamp` ASC, `severity` ASC, `syslog_event_id` ASC),
  INDEX `idx_syslog_unit_id_coll_tms` (`unit_id` ASC, `collector_timestamp` ASC)
)
ENGINE = innodb CHARACTER SET = latin1 COLLATE = latin1_general_ci;

