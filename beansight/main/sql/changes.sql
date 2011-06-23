-- Add to this file the database changes that need to be applied to the %playapps database
-- Reset this page after each release and after having created a SVN tag
--

DROP TABLE `Event`;

CREATE TABLE  `Event` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `callToAction` longtext,
  `creationDate` datetime DEFAULT NULL,
  `endDate` datetime DEFAULT NULL,
  `imageBackgroundLeftURL` varchar(255) DEFAULT NULL,
  `imageBackgroundRightURL` varchar(255) DEFAULT NULL,
  `imageThumbURL` varchar(255) DEFAULT NULL,
  `insight1Tagline` varchar(255) DEFAULT NULL,
  `insight2Tagline` varchar(255) DEFAULT NULL,
  `insight3Tagline` varchar(255) DEFAULT NULL,
  `insight4Tagline` varchar(255) DEFAULT NULL,
  `insight5Tagline` varchar(255) DEFAULT NULL,
  `message` longtext,
  `shareCallToAction` varchar(255) DEFAULT NULL,
  `title` varchar(255) DEFAULT NULL,
  `uniqueId` varchar(255) DEFAULT NULL,
  `insight1_id` bigint(20) DEFAULT NULL,
  `insight2_id` bigint(20) DEFAULT NULL,
  `insight3_id` bigint(20) DEFAULT NULL,
  `insight4_id` bigint(20) DEFAULT NULL,
  `insight5_id` bigint(20) DEFAULT NULL,
  `topic_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK403827AC1F8AB75` (`insight1_id`),
  KEY `FK403827AC1FA0892` (`insight4_id`),
  KEY `FK403827AC1F91FD4` (`insight2_id`),
  KEY `FK403827A45633821` (`topic_id`),
  KEY `FK403827AC1F99433` (`insight3_id`),
  KEY `FK403827AC1FA7CF1` (`insight5_id`),
  KEY `EVENT_UNIQUE_ID_IXD` (`uniqueId`),
  CONSTRAINT `FK403827A45633821` FOREIGN KEY (`topic_id`) REFERENCES `Tag` (`id`),
  CONSTRAINT `FK403827AC1F8AB75` FOREIGN KEY (`insight1_id`) REFERENCES `Insight` (`id`),
  CONSTRAINT `FK403827AC1F91FD4` FOREIGN KEY (`insight2_id`) REFERENCES `Insight` (`id`),
  CONSTRAINT `FK403827AC1F99433` FOREIGN KEY (`insight3_id`) REFERENCES `Insight` (`id`),
  CONSTRAINT `FK403827AC1FA0892` FOREIGN KEY (`insight4_id`) REFERENCES `Insight` (`id`),
  CONSTRAINT `FK403827AC1FA7CF1` FOREIGN KEY (`insight5_id`) REFERENCES `Insight` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;