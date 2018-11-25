DROP TABLE IF EXISTS `monitor_event`;


--
-- Table Definition with Constraints
--
CREATE TABLE `monitor_event`
(
  `event_id` BIGINT NOT NULL AUTO_INCREMENT,
  `module_name` VARCHAR(32) NOT NULL,
  `module_key` VARCHAR(32) NOT NULL,
  `module_state` INTEGER NOT NULL,
  `message` VARCHAR(2000) CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  `starttime` TIMESTAMP NOT NULL,
  `endtime` TIMESTAMP NOT NULL,
  `lastchecked` TIMESTAMP NOT NULL,
  `url` VARCHAR(255),
  PRIMARY KEY (`event_id`),
  CONSTRAINT NameAndKey UNIQUE (module_name,module_key)
)
ENGINE = innodb CHARACTER SET = latin1 COLLATE = latin1_general_ci;
