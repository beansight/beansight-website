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