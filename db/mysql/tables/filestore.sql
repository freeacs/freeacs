DROP TABLE IF EXISTS `filestore`;
CREATE TABLE `filestore` (
  `id` INTEGER NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(64) NOT NULL,
  `unit_type_id` INTEGER NOT NULL,
  `type` VARCHAR(64) NOT NULL DEFAULT 'SOFTWARE',
  `description` VARCHAR(2000) CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  `version` VARCHAR(64) NOT NULL,
  `content` LONGBLOB NOT NULL,
  `timestamp_` DATETIME NOT NULL,
  `target_name` VARCHAR(128) NULL,
  `owner` INTEGER NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `idx_utpid_t_st_v` (`unit_type_id`, `type`(64), `version`(64)),
  UNIQUE INDEX `idx_filestore_utpid_name` (`unit_type_id`, `name`(64)),
  CONSTRAINT `fk_filestore_unit_type_id` FOREIGN KEY `fk_filestore_unit_type_id` (`unit_type_id`)
    REFERENCES `unit_type` (`unit_type_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_filestore_owner` FOREIGN KEY `fk_filestore_owner` (`owner`)
    REFERENCES `user_` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
)
ENGINE = innodb CHARACTER SET = latin1 COLLATE = latin1_general_ci;