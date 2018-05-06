DROP TABLE IF EXISTS `user_`;
CREATE TABLE `user_` (
  `id` INTEGER NOT NULL AUTO_INCREMENT,
  `username` VARCHAR(64) NOT NULL,
  `secret` VARCHAR(64) NOT NULL,
  `fullname` VARCHAR(64) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `accesslist` VARCHAR(256) NOT NULL,
  `is_admin` INTEGER NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `idx_username` (`username`(64))
)
ENGINE = innodb CHARACTER SET = latin1 COLLATE = latin1_general_ci;