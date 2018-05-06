DROP TABLE IF EXISTS `trigger_release`;
CREATE TABLE `trigger_release` (
  `id` INTEGER NOT NULL AUTO_INCREMENT,
  `trigger_id` INTEGER NOT NULL,
  `no_events` INTEGER NULL,
  `no_events_pr_unit` INTEGER NULL,
  `no_units` INTEGER NULL,
  `first_event_timestamp` DATETIME NOT NULL,
  `release_timestamp` DATETIME NOT NULL,
  `sent_timestamp` DATETIME,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_trigger_release_trigger_id` FOREIGN KEY `fk_trigger_release_trigger_id` (`trigger_id`)
    REFERENCES `trigger_` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
)
ENGINE = innodb CHARACTER SET = latin1 COLLATE = latin1_general_ci;

