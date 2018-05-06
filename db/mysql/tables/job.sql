DROP TABLE IF EXISTS `job`;
CREATE TABLE `job` (
  `job_id` INTEGER NOT NULL AUTO_INCREMENT,
  `job_name` VARCHAR(64) NOT NULL,
  `job_type` VARCHAR(32) NOT NULL,
  `description` VARCHAR(2000) CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  `group_id` INTEGER NOT NULL,
  `unconfirmed_timeout` INTEGER NOT NULL,
  `stop_rules` VARCHAR(255) NULL,
  `status` VARCHAR(32) NOT NULL,
  `completed_no_failure` INTEGER NOT NULL,
  `completed_had_failure` INTEGER NOT NULL,
  `confirmed_failed` INTEGER NOT NULL,
  `unconfirmed_failed` INTEGER NOT NULL,
  `start_timestamp` DATETIME NULL,
  `end_timestamp` DATETIME NULL,
  `firmware_id` INTEGER NULL,
  `job_id_dependency` INTEGER NULL,
  `profile_id` INTEGER NULL,
  `repeat_count` INTEGER NULL,
  `repeat_interval` INTEGER NULL,
  PRIMARY KEY (`job_id`),
  CONSTRAINT `fk_job_group_id` FOREIGN KEY `fk_job_group_id` (`group_id`)
    REFERENCES `group_` (`group_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_job_firmware_id` FOREIGN KEY `fk_job_filestore_id` (`firmware_id`)
    REFERENCES `filestore` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_job_job_id` FOREIGN KEY `fk_job_job_id` (`job_id_dependency`)
    REFERENCES `job` (`job_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_job_profile_id` FOREIGN KEY `fk_job_profile_id` (`profile_id`)
    REFERENCES `profile` (`profile_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION

)
ENGINE = innodb CHARACTER SET = latin1 COLLATE = latin1_general_ci;

