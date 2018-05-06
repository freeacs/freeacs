DROP TABLE IF EXISTS `unit`;
CREATE TABLE `unit` (
  `unit_id` VARCHAR(64) NOT NULL,
  `unit_type_id` INTEGER NOT NULL,
  `profile_id` INTEGER NOT NULL,
  PRIMARY KEY (`unit_id`),
  INDEX `idx_unit_unit_type_profile` (`unit_type_id`, `profile_id`, `unit_id`),
  INDEX `idx_unit_profile_unit_type` (`profile_id`, `unit_type_id`, `unit_id`),
  CONSTRAINT `fk_unit_unit_type_id` FOREIGN KEY `fk_unit_unit_type_id` (`unit_type_id`)
    REFERENCES `unit_type` (`unit_type_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_unit_profile_id` FOREIGN KEY `fk_unit_profile_id` (`profile_id`)
    REFERENCES `profile` (`profile_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
)
ENGINE = innodb CHARACTER SET = latin1 COLLATE = latin1_general_ci;

