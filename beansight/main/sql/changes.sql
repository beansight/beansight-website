-- Add to this file the database changes that need to be applied to the %playapps database
-- Reset this page after each release and after having created a SVN tag
--

CREATE TABLE  `Event` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `creationDate` datetime DEFAULT NULL,
  `endDate` datetime DEFAULT NULL,
  `message` longtext,
  `callToAction` longtext,  
  `title` varchar(255) DEFAULT NULL,
  `uniqueId` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `EVENT_UNIQUE_ID_IXD` (`uniqueId`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;

CREATE TABLE  `Event_Insight` (
  `Event_id` bigint(20) NOT NULL,
  `insights_id` bigint(20) NOT NULL,
  KEY `FK1E6FE2B3C216ABF3` (`insights_id`),
  KEY `FK1E6FE2B3BAFEA2D6` (`Event_id`),
  CONSTRAINT `FK1E6FE2B3BAFEA2D6` FOREIGN KEY (`Event_id`) REFERENCES `Event` (`id`),
  CONSTRAINT `FK1E6FE2B3C216ABF3` FOREIGN KEY (`insights_id`) REFERENCES `Insight` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;