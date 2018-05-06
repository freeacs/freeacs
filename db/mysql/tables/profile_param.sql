DROP TABLE IF EXISTS `profile_param`;
CREATE TABLE `profile_param` (
  `profile_id` INTEGER NOT NULL,
  `unit_type_param_id` INTEGER NOT NULL,
  `value` VARCHAR(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  PRIMARY KEY (`profile_id`, `unit_type_param_id`),
  CONSTRAINT `fk_profile_param_profile_id` FOREIGN KEY `fk_profile_param_profile_id` (`profile_id`)
    REFERENCES `profile` (`profile_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_profile_param_u_t_p_id` FOREIGN KEY `fk_profile_param_u_t_p_id` (`unit_type_param_id`)
    REFERENCES `unit_type_param` (`unit_type_param_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
)
ENGINE = innodb CHARACTER SET = latin1 COLLATE = latin1_general_ci;

