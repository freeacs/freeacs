DROP TABLE IF EXISTS `certificate`;
CREATE TABLE `certificate` (
  `id` INTEGER NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(64) NOT NULL,
  `certificate` VARCHAR(256) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `idx_name` (`name`(64))
)
ENGINE = innodb CHARACTER SET = latin1 COLLATE = latin1_general_ci;