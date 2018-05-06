DROP TABLE IF EXISTS `message`;
CREATE TABLE `message` (
  `id` INTEGER NOT NULL AUTO_INCREMENT,
  `type` VARCHAR(64) NOT NULL,
  `sender` VARCHAR(64) NOT NULL,
  `receiver` VARCHAR(64),
  `object_type` VARCHAR(64),
  `object_id` VARCHAR(64),
  `timestamp_` DATETIME NOT NULL,
  `content` VARCHAR(2000) CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  PRIMARY KEY (`id`)
)
ENGINE = innodb CHARACTER SET = latin1 COLLATE = latin1_general_ci;
