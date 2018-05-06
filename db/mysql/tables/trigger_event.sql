DROP TABLE IF EXISTS `trigger_event`;
CREATE TABLE `trigger_event` (
  `id` INTEGER NOT NULL AUTO_INCREMENT,
  `timestamp_` DATETIME NOT NULL,
  `trigger_id` INTEGER NOT NULL,
  `unit_id` VARCHAR(64) NOT NULL, -- We skip foreign key referenec on unit -> increase performance
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_trigger_event_trigger_id` FOREIGN KEY `fk_trigger_event_trigger_id` (`trigger_id`)
    REFERENCES `trigger_` (`id`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
ENGINE = innodb CHARACTER SET = latin1 COLLATE = latin1_general_ci;

