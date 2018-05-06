DROP TABLE IF EXISTS `profile`;
CREATE TABLE `profile` (
  `profile_id` INTEGER NOT NULL AUTO_INCREMENT,
  `unit_type_id` INTEGER NOT NULL,
  `profile_name` VARCHAR(64) NOT NULL,
  PRIMARY KEY (`profile_id`),
  UNIQUE INDEX `idx_unit_type_id_profile_name` (`unit_type_id`, `profile_name`(64)),
  CONSTRAINT `fk_profile_unit_type_id` FOREIGN KEY `fk_profile_unit_type_id` (`unit_type_id`)
    REFERENCES `unit_type` (`unit_type_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
)
ENGINE = innodb CHARACTER SET = latin1 COLLATE = latin1_general_ci;

