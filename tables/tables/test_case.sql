DROP TABLE IF EXISTS `test_case`;
CREATE TABLE `test_case` (
  `id` INTEGER NOT NULL AUTO_INCREMENT,
  `unit_type_id` INTEGER NOT NULL,
  `method` VARCHAR(16) NOT NULL, -- ATTRIBUTE, VALUE
  `tag` VARCHAR(128), -- All tags must be enclosed with [], example: [WIFI] [GENERATED]
  `expect_error` INTEGER, -- 0 NO, 1 YES, NULL UNKNOWN
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_test_case_unit_type_id` FOREIGN KEY `fk_test_case_unit_type_id` (`unit_type_id`)
    REFERENCES `unit_type` (`unit_type_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION

)
ENGINE = innodb CHARACTER SET = latin1 COLLATE = latin1_general_ci;