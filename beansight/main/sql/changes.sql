-- Add to this file the database changes that need to be applied to the %playapps database
-- Reset this page after each release and after having created a SVN tag
--

DROP TABLE `TopicActivity`;
DROP TABLE `Topic_Tag`;
DROP TABLE `User_Topic`;
DROP TABLE `UserTopicVisit`;


ALTER TABLE `User` ADD COLUMN `isDangerous` BIT(1)  NOT NULL;

CREATE TABLE  `User_Tag` (
  `User_id` bigint(20) NOT NULL,
  `followedTopics_id` bigint(20) NOT NULL,
  PRIMARY KEY (`User_id`,`followedTopics_id`),
  KEY `FKF3FCE926E3049DFC` (`followedTopics_id`),
  KEY `FKF3FCE92647140EFE` (`User_id`),
  CONSTRAINT `FKF3FCE92647140EFE` FOREIGN KEY (`User_id`) REFERENCES `User` (`id`),
  CONSTRAINT `FKF3FCE926E3049DFC` FOREIGN KEY (`followedTopics_id`) REFERENCES `Tag` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

ALTER TABLE `Tag` ADD COLUMN `deleted` BIT(1)  NOT NULL;

CREATE TABLE  `Tag_Tag` (
  `Tag_id` bigint(20) NOT NULL,
  `children_id` bigint(20) NOT NULL,
  KEY `FK6E9713549EBA9D6` (`Tag_id`),
  KEY `FK6E9713566043551` (`children_id`),
  CONSTRAINT `FK6E9713549EBA9D6` FOREIGN KEY (`Tag_id`) REFERENCES `Tag` (`id`),
  CONSTRAINT `FK6E9713566043551` FOREIGN KEY (`children_id`) REFERENCES `Tag` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE  `FeaturedTag` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `endDate` datetime DEFAULT NULL,
  `startDate` datetime DEFAULT NULL,
  `language_id` bigint(20) DEFAULT NULL,
  `tag_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK4CF8746C49EBA9D6` (`tag_id`),
  KEY `FK4CF8746CF53795DE` (`language_id`),
  CONSTRAINT `FK4CF8746C49EBA9D6` FOREIGN KEY (`tag_id`) REFERENCES `Tag` (`id`),
  CONSTRAINT `FK4CF8746CF53795DE` FOREIGN KEY (`language_id`) REFERENCES `Language` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;

CREATE TABLE  `TagActivity` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created` datetime DEFAULT NULL,
  `newInsightCount` bigint(20) NOT NULL,
  `notEmpty` bit(1) NOT NULL,
  `totalCount` bigint(20) NOT NULL,
  `updated` datetime DEFAULT NULL,
  `topic_id` bigint(20) DEFAULT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  `tag_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKB38D84A947140EFE` (`user_id`),
  KEY `FKB38D84A945633821` (`topic_id`),
  KEY `FKB38D84A949EBA9D6` (`tag_id`),
  CONSTRAINT `FKB38D84A945633821` FOREIGN KEY (`topic_id`) REFERENCES `Tag` (`id`),
  CONSTRAINT `FKB38D84A947140EFE` FOREIGN KEY (`user_id`) REFERENCES `User` (`id`),
  CONSTRAINT `FKB38D84A949EBA9D6` FOREIGN KEY (`tag_id`) REFERENCES `Tag` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;

CREATE TABLE  `UserTopicVisit` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `application` varchar(255) DEFAULT NULL,
  `creationDate` datetime DEFAULT NULL,
  `ip` varchar(255) DEFAULT NULL,
  `userAgent` longtext,
  `user_id` bigint(20) DEFAULT NULL,
  `topic_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKBF0207A747140EFE` (`user_id`),
  KEY `FKBF0207A745633821` (`topic_id`),
  CONSTRAINT `FKBF0207A745633821` FOREIGN KEY (`topic_id`) REFERENCES `Tag` (`id`),
  CONSTRAINT `FKBF0207A747140EFE` FOREIGN KEY (`user_id`) REFERENCES `User` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;

CREATE TABLE `ApiAccessTokenStore` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `accessToken` varchar(255) DEFAULT NULL,
  `crdate` datetime DEFAULT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK99DAD90647140EFE` (`user_id`),
  KEY `API_USER_TOKEN_IDX` (`accessToken`),
  CONSTRAINT `FK99DAD90647140EFE` FOREIGN KEY (`user_id`) REFERENCES `User` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

ALTER TABLE `User` ADD COLUMN `successfulPredictionCount` INT(11)  NOT NULL;