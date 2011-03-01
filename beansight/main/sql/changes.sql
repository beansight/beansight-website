-- Add to this file the database changes that need to be applied to the %playapps database
-- Reset this page after each release and after having created a SVN tag
--

ALTER TABLE `ContactMailTask` MODIFY COLUMN `message` LONGTEXT  CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL;

CREATE TABLE `UserPromocodeCampaign` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `application` varchar(255) DEFAULT NULL,
  `creationDate` datetime DEFAULT NULL,
  `ip` varchar(255) DEFAULT NULL,
  `userAgent` varchar(255) DEFAULT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  `promocode_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK8E27D40147140EFE` (`user_id`),
  KEY `FK8E27D4019F8BD196` (`promocode_id`),
  CONSTRAINT `FK8E27D4019F8BD196` FOREIGN KEY (`promocode_id`) REFERENCES `Promocode` (`id`),
  CONSTRAINT `FK8E27D40147140EFE` FOREIGN KEY (`user_id`) REFERENCES `User` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- New tables for score computation:
CREATE TABLE  `InsightTrend` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `agreeCount` bigint(20) NOT NULL,
  `disagreeCount` bigint(20) NOT NULL,  
  `occurenceProbability` double NOT NULL,
  `trendDate` datetime DEFAULT NULL,
  `insight_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKCCE4A9057BBEB316` (`insight_id`),
  CONSTRAINT `FKCCE4A9057BBEB316` FOREIGN KEY (`insight_id`) REFERENCES `Insight` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE  `UserInsightScore` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `lastUpdate` datetime DEFAULT NULL,
  `score` double NOT NULL,
  `insight_id` bigint(20) DEFAULT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK62A8BD657BBEB316` (`insight_id`),
  KEY `FK62A8BD6547140EFE` (`user_id`),
  CONSTRAINT `FK62A8BD6547140EFE` FOREIGN KEY (`user_id`) REFERENCES `User` (`id`),
  CONSTRAINT `FK62A8BD657BBEB316` FOREIGN KEY (`insight_id`) REFERENCES `Insight` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

ALTER TABLE `UserCategoryScore` ADD COLUMN `normalizedScore` DOUBLE  NOT NULL DEFAULT 0 AFTER `score`;
ALTER TABLE `User` ADD COLUMN `lastScoreUpdate` DATETIME  DEFAULT NULL AFTER `score`;
ALTER TABLE `Category` ADD COLUMN `scoreMax` DOUBLE  NOT NULL DEFAULT 0 AFTER `label`;
ALTER TABLE `Category` ADD COLUMN `scoreMin` DOUBLE  NOT NULL DEFAULT 0 AFTER `label`;

ALTER TABLE `User` ADD COLUMN `secondWrittingLanguage_id` BIGINT(20)  DEFAULT NULL AFTER `writtingLanguage_id`;

