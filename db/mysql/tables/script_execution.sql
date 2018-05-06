DROP TABLE IF EXISTS `script_execution`;
CREATE TABLE `script_execution` (
  `id` INTEGER NOT NULL AUTO_INCREMENT,
  `unit_type_id` INTEGER NOT NULL, -- SET BY REQUEST-CLIENT
  `filestore_id` INTEGER NOT NULL, -- SET BY REQUEST-CLIENT
  `arguments` VARCHAR(1024), -- SET BY REQUEST-CLIENT
  `request_timestamp` DATETIME NOT NULL, -- SET BY REQUEST-CLIENT
  `request_id` VARCHAR(32), -- SET BY REQUEST-CLIENT
  `start_timestamp` DATETIME, -- SET BY SSD
  `end_timestamp` DATETIME, -- SET BY SSD
  `exit_status` INTEGER, -- SET BY SSD (0=SUCCESS, 1=ERROR)
  `error_message` VARCHAR(1024), -- SET BY SSD IF NECESSARY
  PRIMARY KEY (`id`)
)
ENGINE = innodb CHARACTER SET = latin1 COLLATE = latin1_general_ci;

