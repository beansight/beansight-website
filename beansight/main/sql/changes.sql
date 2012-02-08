-- Add to this file the database changes that need to be applied to the %playapps database
-- Reset this page after each release and after having created a SVN tag
--

CREATE TABLE  `InsightSuggest` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `becauseFollowedUserCreated` bit(1) NOT NULL,
  `created` datetime DEFAULT NULL,
  `endDate` datetime DEFAULT NULL,
  `score` double NOT NULL,
  `updated` datetime DEFAULT NULL,
  `insight_id` bigint(20) DEFAULT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKF6AFDCEC7BBEB316` (`insight_id`),
  KEY `FKF6AFDCEC47140EFE` (`user_id`),
  CONSTRAINT `FKF6AFDCEC47140EFE` FOREIGN KEY (`user_id`) REFERENCES `User` (`id`),
  CONSTRAINT `FKF6AFDCEC7BBEB316` FOREIGN KEY (`insight_id`) REFERENCES `Insight` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=58 DEFAULT CHARSET=utf8;

CREATE TABLE  `InsightSuggest_Tag` (
  `InsightSuggest_id` bigint(20) NOT NULL,
  `becauseFollowedTag_id` bigint(20) NOT NULL,
  KEY `FK308BFDA727F6A3E` (`InsightSuggest_id`),
  KEY `FK308BFDA71F40956C` (`becauseFollowedTag_id`),
  CONSTRAINT `FK308BFDA71F40956C` FOREIGN KEY (`becauseFollowedTag_id`) REFERENCES `Tag` (`id`),
  CONSTRAINT `FK308BFDA727F6A3E` FOREIGN KEY (`InsightSuggest_id`) REFERENCES `InsightSuggest` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE  `InsightSuggest_User` (
  `InsightSuggest_id` bigint(20) NOT NULL,
  `becauseFollowedUserVoted_id` bigint(20) NOT NULL,
  KEY `FKE0F46F5E27F6A3E` (`InsightSuggest_id`),
  KEY `FKE0F46F5E99272990` (`becauseFollowedUserVoted_id`),
  CONSTRAINT `FKE0F46F5E27F6A3E` FOREIGN KEY (`InsightSuggest_id`) REFERENCES `InsightSuggest` (`id`),
  CONSTRAINT `FKE0F46F5E99272990` FOREIGN KEY (`becauseFollowedUserVoted_id`) REFERENCES `User` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;