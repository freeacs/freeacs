DROP TABLE IF EXISTS `syslog_event`;
CREATE TABLE `syslog_event` (
  `id` INTEGER NOT NULL AUTO_INCREMENT,
  `syslog_event_id` INTEGER NOT NULL,
  `syslog_event_name` VARCHAR(64) NOT NULL,
  `unit_type_id` INTEGER NOT NULL,
  `group_id` INTEGER NULL,
  `expression` VARCHAR(64) NOT NULL DEFAULT 'Specify an expression', 
  `store_policy` VARCHAR(16) NOT NULL DEFAULT 'STORE',
  `filestore_id` INTEGER,
  `description` VARCHAR(1024) CHARACTER SET utf8 COLLATE utf8_general_ci,
  `delete_limit` INTEGER,
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
)
ENGINE = innodb CHARACTER SET = latin1 COLLATE = latin1_general_ci;

