DROP TABLE IF EXISTS `unit_param_session`;
CREATE TABLE `unit_param_session` (
  `unit_id` VARCHAR(64) NOT NULL,
  `unit_type_param_id` INTEGER NOT NULL,
  `value` VARCHAR(512) CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  PRIMARY KEY (`unit_id`, `unit_type_param_id`),
  CONSTRAINT `fk_unit_param_session_unit_id` FOREIGN KEY `fk_unit_param_session_unit_id` (`unit_id`)
    REFERENCES `unit` (`unit_id`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_unit_param_session_u_t_p_id` FOREIGN KEY `fk_unit_param_session_u_t_p_id` (`unit_type_param_id`)
    REFERENCES `unit_type_param` (`unit_type_param_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
)
ENGINE = innodb CHARACTER SET = latin1 COLLATE = latin1_general_ci;

