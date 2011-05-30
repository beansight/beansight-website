-- Add to this file the database changes that need to be applied to the %playapps database
-- Reset this page after each release and after having created a SVN tag
--

ALTER TABLE `User` ADD COLUMN `sponsor` BIT(1)  NOT NULL DEFAULT 0;

ALTER TABLE `Insight`
    ADD COLUMN `sponsored` BIT(1) NOT NULL DEFAULT 0,
    ADD COLUMN `sponsor_id` bigint(20) DEFAULT NULL,
    ADD KEY `FKD7E1D3787AE6314F` (`sponsor_id`),
    ADD CONSTRAINT `FKD7E1D3787AE6314F` FOREIGN KEY (`sponsor_id`) REFERENCES `User` (`id`);


CREATE TABLE  `FeaturedSponsor` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `endDate` datetime DEFAULT NULL,
  `sponsor` tinyblob,
  `startDate` datetime DEFAULT NULL,
  `language_id` bigint(20) DEFAULT NULL,
  `sponsor_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK5EE74EECF53795DE` (`language_id`),
  KEY `FK5EE74EEC7AE6314F` (`sponsor_id`),
  CONSTRAINT `FK5EE74EEC7AE6314F` FOREIGN KEY (`sponsor_id`) REFERENCES `User` (`id`),
  CONSTRAINT `FK5EE74EECF53795DE` FOREIGN KEY (`language_id`) REFERENCES `Language` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;

CREATE TABLE  `FeaturedSponsor_Insight` (
  `FeaturedSponsor_id` bigint(20) NOT NULL,
  `insights_id` bigint(20) NOT NULL,
  KEY `FK75411D25C216ABF3` (`insights_id`),
  KEY `FK75411D2590AF9676` (`FeaturedSponsor_id`),
  CONSTRAINT `FK75411D2590AF9676` FOREIGN KEY (`FeaturedSponsor_id`) REFERENCES `FeaturedSponsor` (`id`),
  CONSTRAINT `FK75411D25C216ABF3` FOREIGN KEY (`insights_id`) REFERENCES `Insight` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE  `FeaturedInsight` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `endDate` datetime DEFAULT NULL,
  `startDate` datetime DEFAULT NULL,
  `insight_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK4AB5376A7BBEB316` (`insight_id`),
  CONSTRAINT `FK4AB5376A7BBEB316` FOREIGN KEY (`insight_id`) REFERENCES `Insight` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;


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