-- Add to this file the database changes that need to be applied to the %playapps database
-- Reset this page after each release and after having created a SVN tag
--

CREATE TABLE  `Topic` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `creationDate` datetime DEFAULT NULL,
  `label` varchar(255) DEFAULT NULL,
  `creator_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK4D3DD0FA21012FD` (`creator_id`),
  CONSTRAINT `FK4D3DD0FA21012FD` FOREIGN KEY (`creator_id`) REFERENCES `User` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;

CREATE TABLE  `Topic_Tag` (
  `Topic_id` bigint(20) NOT NULL,
  `tags_id` bigint(20) NOT NULL,
  KEY `FK8E9D344A222C70F7` (`tags_id`),
  KEY `FK8E9D344A722C1EB6` (`Topic_id`),
  CONSTRAINT `FK8E9D344A722C1EB6` FOREIGN KEY (`Topic_id`) REFERENCES `Topic` (`id`),
  CONSTRAINT `FK8E9D344A222C70F7` FOREIGN KEY (`tags_id`) REFERENCES `Tag` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

ALTER TABLE `User` ADD COLUMN `followMail` BIT(1)  NOT NULL DEFAULT 1;
ALTER TABLE `User` ADD COLUMN `messageMail` BIT(1)  NOT NULL DEFAULT 1;
ALTER TABLE `User` ADD COLUMN `commentCreatedMail` BIT(1)  NOT NULL DEFAULT 1;
ALTER TABLE `User` ADD COLUMN `commentFavoriteMail` BIT(1)  NOT NULL DEFAULT 1;
ALTER TABLE `User` ADD COLUMN `commentCommentMail` BIT(1)  NOT NULL DEFAULT 1;
ALTER TABLE `User` ADD COLUMN `commentMentionMail` BIT(1)  NOT NULL DEFAULT 1;

ALTER TABLE `User` ADD COLUMN `upcomingNewsletter` BIT(1)  NOT NULL DEFAULT 1;
ALTER TABLE `User` ADD COLUMN `statusNewsletter` BIT(1)  NOT NULL DEFAULT 1;

-- update all endDate that are currently finishing at midnight to set them to the same day but at end of day at 23:59:59 :
UPDATE Insight
SET endDate = ADDTIME(endDate, "23:59:59");


CREATE TABLE `CommentNotificationMessage` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `comment_id` bigint(20) DEFAULT NULL,
  `fromUser_id` bigint(20) DEFAULT NULL,
  `insight_id` bigint(20) DEFAULT NULL,
  `toUser_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKE41383DD7BBEB316` (`insight_id`),
  KEY `FKE41383DD80B8D023` (`toUser_id`),
  KEY `FKE41383DD7B83E9B6` (`comment_id`),
  KEY `FKE41383DDE773E414` (`fromUser_id`),
  CONSTRAINT `FKE41383DDE773E414` FOREIGN KEY (`fromUser_id`) REFERENCES `User` (`id`),
  CONSTRAINT `FKE41383DD7B83E9B6` FOREIGN KEY (`comment_id`) REFERENCES `Comment` (`id`),
  CONSTRAINT `FKE41383DD7BBEB316` FOREIGN KEY (`insight_id`) REFERENCES `Insight` (`id`),
  CONSTRAINT `FKE41383DD80B8D023` FOREIGN KEY (`toUser_id`) REFERENCES `User` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;

CREATE TABLE `CommentNotificationMailTask` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `attempt` int(11) NOT NULL,
  `created` datetime DEFAULT NULL,
  `language` varchar(255) DEFAULT NULL,
  `sendTo` varchar(255) DEFAULT NULL,
  `sent` bit(1) NOT NULL,
  `commentNotificationMsg_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKB94AF2C68EE699C4` (`commentNotificationMsg_id`),
  CONSTRAINT `FKB94AF2C68EE699C4` FOREIGN KEY (`commentNotificationMsg_id`) REFERENCES `CommentNotificationMessage` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;