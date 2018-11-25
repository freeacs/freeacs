DROP TABLE IF EXISTS `permission_`;
CREATE TABLE `permission_` (
  `id` INTEGER NOT NULL AUTO_INCREMENT,
  `user_id` INTEGER NOT NULL,
  `unit_type_id` INTEGER NOT NULL,
  `profile_id` INTEGER NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `idx_uid_utpid_pid` (`user_id`,`unit_type_id`,`profile_id`),
CONSTRAINT `fk_permission_user_id` FOREIGN KEY
`fk_permission_user_id`
(`user_id`)
    REFERENCES `user_` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
CONSTRAINT `fk_permission_unit_type_id` FOREIGN KEY
`fk_permission_unit_type_id`
(`unit_type_id`)
    REFERENCES `unit_type` (`unit_type_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
)
ENGINE = innodb CHARACTER SET = latin1 COLLATE = latin1_general_ci;