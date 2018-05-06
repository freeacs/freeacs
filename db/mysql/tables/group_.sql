DROP TABLE IF EXISTS `group_`;
CREATE TABLE `group_` (
  `group_id` INTEGER NOT NULL AUTO_INCREMENT,
  `unit_type_id` INTEGER NOT NULL,
  `group_name` VARCHAR(64) NOT NULL,
  `description` VARCHAR(2000) CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  `parent_group_id` INTEGER NULL,
  `profile_id` INTEGER NULL,
  `count` INTEGER NULL, 
  `time_param_id` INTEGER NULL,
  `time_rolling_rule` VARCHAR(32) NULL,
  PRIMARY KEY (`group_id`),
  CONSTRAINT `fk_group__unit_type_id` FOREIGN KEY `fk_group__unit_type_id` (`unit_type_id`)
    REFERENCES `unit_type` (`unit_type_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_group__group_id` FOREIGN KEY `fk_group__group_id` (`parent_group_id`)
    REFERENCES `group_` (`group_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_group__profile_id` FOREIGN KEY `fk_group__profile_id` (`profile_id`)
    REFERENCES `profile` (`profile_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_time_param_u_t_p_id` FOREIGN KEY `fk_time_param_u_t_p_id` (`time_param_id`)
    REFERENCES `unit_type_param` (`unit_type_param_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
)
ENGINE = innodb CHARACTER SET = latin1 COLLATE = latin1_general_ci;

