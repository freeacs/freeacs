DROP TABLE IF EXISTS `unit_job`;
CREATE TABLE `unit_job` (
  `unit_id` VARCHAR(64) NOT NULL,
  `job_id`INTEGER NOT NULL,
  `start_timestamp` DATETIME NOT NULL,
  `end_timestamp` DATETIME NULL,
  `status` VARCHAR(32) NOT NULL,
  `processed` INTEGER NULL DEFAULT '0',
  `confirmed` INTEGER NULL DEFAULT '0',
  `unconfirmed` INTEGER NULL DEFAULT '0',
  PRIMARY KEY (`unit_id`, `job_id`),
  CONSTRAINT `fk_unit_job_unit_id` FOREIGN KEY `fk_unit_job_unit_id` (`unit_id`)
    REFERENCES `unit` (`unit_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_unit_job_job_id` FOREIGN KEY `fk_unit_job_job_id` (`job_id`)
    REFERENCES `job` (`job_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  INDEX `idx_unit_job_1` (`status`(32), `start_timestamp`),
  INDEX `idx_unit_job_2` (`processed`)
)
ENGINE = innodb CHARACTER SET = latin1 COLLATE = latin1_general_ci;

