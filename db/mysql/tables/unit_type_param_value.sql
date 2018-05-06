DROP TABLE IF EXISTS `unit_type_param_value`;
CREATE TABLE `unit_type_param_value` (
  `unit_type_param_id` INTEGER NOT NULL,
  `value` VARCHAR(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `priority` INTEGER NOT NULL,
  `type` VARCHAR(32) NOT NULL DEFAULT 'enum',
  PRIMARY KEY (`unit_type_param_id`,`value`),
  CONSTRAINT `fk_unit_param_value_utpid` FOREIGN KEY `fk_unit_param_value_utpid` (`unit_type_param_id`)
    REFERENCES `unit_type_param` (`unit_type_param_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
)
ENGINE = innodb CHARACTER SET = latin1 COLLATE = latin1_general_ci;
