DROP TABLE IF EXISTS `group_param`;
CREATE TABLE `group_param` (
	`id` INTEGER NOT NULL AUTO_INCREMENT,
  `group_id` INTEGER NOT NULL,
  `unit_type_param_id` INTEGER NOT NULL,
  `operator` VARCHAR(2) NOT NULL DEFAULT '=',
  `data_type` VARCHAR(32) NOT NULL DEFAULT 'TEXT',  
  `value` VARCHAR(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_group_param_group_id` FOREIGN KEY `fk_group_param_group_id` (`group_id`)
    REFERENCES `group_` (`group_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_group_param_u_t_p_id` FOREIGN KEY `fk_group_param_u_t_p_id` (`unit_type_param_id`)
    REFERENCES `unit_type_param` (`unit_type_param_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
)
ENGINE = innodb CHARACTER SET = latin1 COLLATE = latin1_general_ci;

