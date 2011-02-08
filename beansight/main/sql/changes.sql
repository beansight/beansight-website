-- Add to this file the database changes that need to be applied to the %playapps database
-- Reset this page after each release and after having created a SVN tag
--

CREATE TABLE  `beansight`.`UserInsightVisit` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `application` varchar(255) DEFAULT NULL,
  `ip` varchar(255) DEFAULT NULL,
  `timestamp` datetime DEFAULT NULL,
  `userAgent` varchar(255) DEFAULT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  `insight_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK62D5CC1E7BBEB316` (`insight_id`),
  KEY `FK62D5CC1E47140EFE` (`user_id`),
  CONSTRAINT `FK62D5CC1E47140EFE` FOREIGN KEY (`user_id`) REFERENCES `User` (`id`),
  CONSTRAINT `FK62D5CC1E7BBEB316` FOREIGN KEY (`insight_id`) REFERENCES `Insight` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;


CREATE TABLE `UserExpertVisit` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `application` varchar(255) DEFAULT NULL,
  `ip` varchar(255) DEFAULT NULL,
  `timestamp` datetime DEFAULT NULL,
  `userAgent` varchar(255) DEFAULT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  `expert_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKAA87705647140EFE` (`user_id`),
  KEY `FKAA87705657F5FC3F` (`expert_id`),
  CONSTRAINT `FKAA87705657F5FC3F` FOREIGN KEY (`expert_id`) REFERENCES `user` (`id`),
  CONSTRAINT `FKAA87705647140EFE` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;


CREATE TABLE `UserInsightSearchVisit` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `application` varchar(255) DEFAULT NULL,
  `ip` varchar(255) DEFAULT NULL,
  `timestamp` datetime DEFAULT NULL,
  `userAgent` varchar(255) DEFAULT NULL,
  `searchKeyWords` varchar(255) DEFAULT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKB6AA151647140EFE` (`user_id`),
  CONSTRAINT `FKB6AA151647140EFE` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8;


CREATE TABLE `UserListExpertsVisit` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `application` varchar(255) DEFAULT NULL,
  `ip` varchar(255) DEFAULT NULL,
  `timestamp` datetime DEFAULT NULL,
  `userAgent` varchar(255) DEFAULT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK62C0490B47140EFE` (`user_id`),
  CONSTRAINT `FK62C0490B47140EFE` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;


CREATE TABLE `UserListInsightsVisit` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `application` varchar(255) DEFAULT NULL,
  `ip` varchar(255) DEFAULT NULL,
  `timestamp` datetime DEFAULT NULL,
  `userAgent` varchar(255) DEFAULT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK1A0C198747140EFE` (`user_id`),
  CONSTRAINT `FK1A0C198747140EFE` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8;
