-- Add to this file the database changes that need to be applied to the %playapps database
-- Reset this page after each release and after having created a SVN tag
--

ALTER TABLE `Insight` ADD COLUMN `fakeValidationScore` DOUBLE  DEFAULT NULL;

delete from `UserCategoryScore`;

alter table UserCategoryScore modify column score double default null;
alter table UserCategoryScore modify column normalizedScore double default null;

alter table UserInsightScore modify column score double default null;

alter table User modify column score double default null;

update UserInsightScore
set score = null
where score = 0;

CREATE TABLE `UserScoreHistoric` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `scoreDate` datetime DEFAULT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `scoreDate` (`scoreDate`,`user_id`),
  KEY `FKD938245C47140EFE` (`user_id`),
  CONSTRAINT `FKD938245C47140EFE` FOREIGN KEY (`user_id`) REFERENCES `User` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


ALTER TABLE `UserCategoryScore` 
ADD COLUMN `period` varchar(255) DEFAULT NULL;

ALTER TABLE `UserCategoryScore` 
ADD COLUMN `historic_id` bigint(20) DEFAULT NULL;

ALTER TABLE `UserCategoryScore`
ADD CONSTRAINT `FK68634609C6352FD3` FOREIGN KEY (`historic_id`) REFERENCES `UserScoreHistoric` (`id`);


ALTER TABLE `UserCategoryScore` DROP FOREIGN KEY `FK6863460947140EFE`;
ALTER TABLE `UserCategoryScore` DROP COLUMN `user_id`;

update User set statusNewsletter = true;

CREATE TABLE  `WeeklyMailingTask` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `attempt` int(11) NOT NULL,
  `created` datetime DEFAULT NULL,
  `language` varchar(255) DEFAULT NULL,
  `sendTo` varchar(255) DEFAULT NULL,
  `sent` bit(1) NOT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKF56DAB8F47140EFE` (`user_id`),
  CONSTRAINT `FKF56DAB8F47140EFE` FOREIGN KEY (`user_id`) REFERENCES `User` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE  `WeeklyMailingTask_NextWeekInsights` (
  `WeeklyMailingTask_id` bigint(20) NOT NULL,
  `nextWeekInsights_id` bigint(20) NOT NULL,
  KEY `FK1BA2BD52F26200B6` (`WeeklyMailingTask_id`),
  KEY `FK1BA2BD52E7041C2C` (`nextWeekInsights_id`),
  CONSTRAINT `FK1BA2BD52E7041C2C` FOREIGN KEY (`nextWeekInsights_id`) REFERENCES `Insight` (`id`),
  CONSTRAINT `FK1BA2BD52F26200B6` FOREIGN KEY (`WeeklyMailingTask_id`) REFERENCES `WeeklyMailingTask` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE  `WeeklyMailingTask_PreviousWeekInsights` (
  `WeeklyMailingTask_id` bigint(20) NOT NULL,
  `previousWeekInsights_id` bigint(20) NOT NULL,
  KEY `FK2DD51F56F26200B6` (`WeeklyMailingTask_id`),
  KEY `FK2DD51F56D70563A8` (`previousWeekInsights_id`),
  CONSTRAINT `FK2DD51F56D70563A8` FOREIGN KEY (`previousWeekInsights_id`) REFERENCES `Insight` (`id`),
  CONSTRAINT `FK2DD51F56F26200B6` FOREIGN KEY (`WeeklyMailingTask_id`) REFERENCES `WeeklyMailingTask` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

update InsightActivity set newAgreeCount = 0, newDisagreeCount = 0, newFavoriteCount = 0, voteChangeCount = 0, newCommentCount = 0, totalCount = 0, notEmpty = false;