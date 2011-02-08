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
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8