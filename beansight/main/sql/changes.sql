-- Add to this file the database changes that need to be applied to the %playapps database
-- Reset this page after each release and after having created a SVN tag
--

CREATE TABLE `User_Topic` (
  `User_id` bigint(20) NOT NULL,
  `followedTopics_id` bigint(20) NOT NULL,
  PRIMARY KEY (`User_id`,`followedTopics_id`),
  KEY `FKE86DC3BBFCD8491` (`followedTopics_id`),
  KEY `FKE86DC3BB47140EFE` (`User_id`),
  CONSTRAINT `FKE86DC3BB47140EFE` FOREIGN KEY (`User_id`) REFERENCES `User` (`id`),
  CONSTRAINT `FKE86DC3BBFCD8491` FOREIGN KEY (`followedTopics_id`) REFERENCES `Topic` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE  `FeaturedTopic` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `endDate` datetime DEFAULT NULL,
  `startDate` datetime DEFAULT NULL,
  `topic_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKF0B39581722C1EB6` (`topic_id`),
  CONSTRAINT `FKF0B39581722C1EB6` FOREIGN KEY (`topic_id`) REFERENCES `Topic` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;

CREATE TABLE  `TopicActivity` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created` datetime DEFAULT NULL,
  `newInsightCount` bigint(20) NOT NULL,
  `notEmpty` bit(1) NOT NULL,
  `totalCount` bigint(20) NOT NULL,
  `updated` datetime DEFAULT NULL,
  `topic_id` bigint(20) DEFAULT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK6924B90247140EFE` (`user_id`),
  KEY `FK6924B902722C1EB6` (`topic_id`),
  CONSTRAINT `FK6924B902722C1EB6` FOREIGN KEY (`topic_id`) REFERENCES `Topic` (`id`),
  CONSTRAINT `FK6924B90247140EFE` FOREIGN KEY (`user_id`) REFERENCES `User` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;

CREATE TABLE  `UserActivity` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created` datetime DEFAULT NULL,
  `newInsightCount` bigint(20) NOT NULL,
  `newVoteCount` bigint(20) NOT NULL,
  `notEmpty` bit(1) NOT NULL,
  `totalCount` bigint(20) NOT NULL,
  `updated` datetime DEFAULT NULL,
  `voteChangeCount` bigint(20) NOT NULL,
  `followedUser_id` bigint(20) DEFAULT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK122B0FD61225BCEE` (`followedUser_id`),
  KEY `FK122B0FD647140EFE` (`user_id`),
  CONSTRAINT `FK122B0FD647140EFE` FOREIGN KEY (`user_id`) REFERENCES `User` (`id`),
  CONSTRAINT `FK122B0FD61225BCEE` FOREIGN KEY (`followedUser_id`) REFERENCES `User` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;

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
  KEY `FKBF0207A7722C1EB6` (`topic_id`),
  CONSTRAINT `FKBF0207A7722C1EB6` FOREIGN KEY (`topic_id`) REFERENCES `Topic` (`id`),
  CONSTRAINT `FKBF0207A747140EFE` FOREIGN KEY (`user_id`) REFERENCES `User` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;