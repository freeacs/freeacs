DROP TABLE IF EXISTS `unit_type_param`;
CREATE TABLE `unit_type_param` (
  `unit_type_param_id` INTEGER NOT NULL AUTO_INCREMENT,
  `unit_type_id` INTEGER NOT NULL,
  `name` VARCHAR(255) NOT NULL,
  `flags` VARCHAR(32) NOT NULL,
  PRIMARY KEY (`unit_type_param_id`),
  UNIQUE INDEX `idx_u_t_p_unit_type_id_name` (`unit_type_id`, `name`(255)),
  CONSTRAINT `fk_u_t_p_unit_type_id` FOREIGN KEY `fk_u_t_p_unit_type_id` (`unit_type_id`)
    REFERENCES `unit_type` (`unit_type_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
)
ENGINE = innodb CHARACTER SET = latin1 COLLATE = latin1_general_ci;

