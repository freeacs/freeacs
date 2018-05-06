DROP TABLE IF EXISTS `heartbeat`;
CREATE TABLE `heartbeat` (
  `id` INTEGER NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(64) NOT NULL,
  `unit_type_id` INTEGER NOT NULL,  
  `heartbeat_expression` VARCHAR(64) NOT NULL,
  `heartbeat_group_id` INTEGER NOT NULL,
  `heartbeat_timeout_hour` INTEGER NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`),
   CONSTRAINT `fk_hb_group_id` FOREIGN KEY `fk_hb_group_id` (`heartbeat_group_id`)
    REFERENCES `group_` (`group_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
 CONSTRAINT `fk_hb_unit_type_id` FOREIGN KEY `fk_hb_unit_type_id` (`unit_type_id`)
    REFERENCES `unit_type` (`unit_type_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
)
ENGINE = innodb CHARACTER SET = latin1 COLLATE = latin1_general_ci;

