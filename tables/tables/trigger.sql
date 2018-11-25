DROP TABLE IF EXISTS `trigger_`;
CREATE TABLE `trigger_` (
  `id` INTEGER NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(255) NOT NULL,
  `description` VARCHAR(1024),
-- BASIC or COMPOSITE (0 or 1)
  `trigger_type` INTEGER NOT NULL DEFAULT 0, 
-- ALARM, REPORT or SILENT (0-2)
  `notify_type` INTEGER NOT NULL DEFAULT 0, 
-- 0 or 1 
  `active` INTEGER NOT NULL DEFAULT 0, 
  `unit_type_id` INTEGER NOT NULL,
  `group_id` INTEGER,
-- 15 to 120 (minutes)
  `eval_period_minutes` INTEGER NOT NULL, 
-- 1 to 168 (hours)
  `notify_interval_hours` INTEGER NULL, 
-- REFERS TO FILESTORE.ID  
  `filestore_id` INTEGER,
-- REFERS TO ID
  `parent_trigger_id` INTEGER, 
  `to_list` VARCHAR(512),
-- JUST FOR BASIC
  `syslog_event_id` INTEGER, 
  `no_events` INTEGER, 
  `no_events_pr_unit` INTEGER,
  `no_units` INTEGER, 
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
)
ENGINE = innodb CHARACTER SET = latin1 COLLATE = latin1_general_ci;

