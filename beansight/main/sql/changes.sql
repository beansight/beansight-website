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