-- Add to this file the database changes that need to be applied to the %playapps database
-- Reset this page after each release and after having created a SVN tag
--

CREATE TABLE `ComputeScoreForUsersTask` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `computeDate` datetime DEFAULT NULL,
  `period` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


CREATE TABLE `ComputeScoreForUsersTask_User` (
  `ComputeScoreForUsersTask_id` bigint(20) NOT NULL,
  `users_id` bigint(20) NOT NULL,
  KEY `FKFA9876EB4004E7A1` (`users_id`),
  KEY `FKFA9876EB6F20A38F` (`ComputeScoreForUsersTask_id`),
  CONSTRAINT `FKFA9876EB6F20A38F` FOREIGN KEY (`ComputeScoreForUsersTask_id`) REFERENCES `ComputeScoreForUsersTask` (`id`),
  CONSTRAINT `FKFA9876EB4004E7A1` FOREIGN KEY (`users_id`) REFERENCES `User` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;