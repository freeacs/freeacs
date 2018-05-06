DROP TABLE IF EXISTS `test_case_files`;
CREATE TABLE `test_case_files` (
  `id` INTEGER NOT NULL AUTO_INCREMENT,
  `case_id` INTEGER NOT NULL,
  `input_file_id` INTEGER NOT NULL,
  `output_file_id` INTEGER,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_test_case_files_input_filestore_id` FOREIGN KEY `fk_test_case_files_input_filestore_id` (`input_file_id`)
    REFERENCES `filestore` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_test_case_files_output_filestore_id` FOREIGN KEY `fk_test_case_files_output_filestore_id` (`output_file_id`)
    REFERENCES `filestore` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_test_case_files_case_id` FOREIGN KEY `fk_test_case_files_case_id` (`case_id`)
    REFERENCES `test_case` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION

)
ENGINE = innodb CHARACTER SET = latin1 COLLATE = latin1_general_ci;