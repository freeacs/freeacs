DROP TABLE IF EXISTS `test_history`;
CREATE TABLE `test_history` (
  `id` INTEGER NOT NULL AUTO_INCREMENT,
  `unit_type_id` INTEGER NOT NULL,
  `unit_id` VARCHAR(64) NOT NULL,
  `case_id` INTEGER NOT NULL,
  `start_timestamp` DATETIME NOT NULL,
  `end_timestamp` DATETIME,
  `failed` INTEGER NOT NULL DEFAULT 0,
  `expect_error` INTEGER NOT NULL DEFAULT 0, 
  `result` VARCHAR(4096),
  PRIMARY KEY (`id`)
)
ENGINE = innodb CHARACTER SET = latin1 COLLATE = latin1_general_ci;