DROP TABLE IF EXISTS `job_param`;
CREATE TABLE `job_param` (
  `job_id` INTEGER NOT NULL,
  `unit_id` VARCHAR(64) NOT NULL,
  `unit_type_param_id` INTEGER NOT NULL,
  `value` VARCHAR(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  PRIMARY KEY (`job_id`, `unit_id`, `unit_type_param_id`),
  CONSTRAINT `fk_job_param_job_id` FOREIGN KEY `fk_job_param_job_id` (`job_id`)
    REFERENCES `job` (`job_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_job_param_u_t_p_id` FOREIGN KEY `fk_job_param_u_t_p_id` (`unit_type_param_id`)
    REFERENCES `unit_type_param` (`unit_type_param_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
)
ENGINE = innodb CHARACTER SET = latin1 COLLATE = latin1_general_ci;

