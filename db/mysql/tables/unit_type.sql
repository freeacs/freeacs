DROP TABLE IF EXISTS `unit_type`;
CREATE TABLE `unit_type` (
  `unit_type_id` INTEGER NOT NULL AUTO_INCREMENT,
  `matcher_id` VARCHAR(32) NULL,
  `unit_type_name` VARCHAR(64) NOT NULL,
  `vendor_name` VARCHAR(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  `description` VARCHAR(2000) CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  `protocol`    VARCHAR(16) NOT NULL,
  PRIMARY KEY (`unit_type_id`),
  UNIQUE INDEX `uq_unit_type_name` (`unit_type_name`(64))
)
ENGINE = innodb CHARACTER SET = latin1 COLLATE = latin1_general_ci;

