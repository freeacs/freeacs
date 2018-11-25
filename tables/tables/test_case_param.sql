DROP TABLE IF EXISTS `test_case_param`;
CREATE TABLE `test_case_param` (
  `id` INTEGER NOT NULL AUTO_INCREMENT,
  `type` VARCHAR(16) NOT NULL, -- IN, OUT
  `case_id` INTEGER NOT NULL,
  `unit_type_param_id` INTEGER NOT NULL,
  `value` VARCHAR(512) CHARACTER SET utf8 COLLATE utf8_general_ci,
  `notification` INTEGER,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_test_case_param_input_u_t_p_id` FOREIGN KEY `fk_test_case_param_input_u_t_p_id` (`unit_type_param_id`)
    REFERENCES `unit_type_param` (`unit_type_param_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_test_case_param_input_case_id` FOREIGN KEY `fk_test_case_param_input_case_id` (`case_id`)
    REFERENCES `test_case` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION

)
ENGINE = innodb CHARACTER SET = latin1 COLLATE = latin1_general_ci;