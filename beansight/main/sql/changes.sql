-- Add to this file the database changes that need to be applied to the %playapps database
-- Reset this page after each release and after having created a SVN tag
--

ALTER TABLE `User` ADD COLUMN `insightShareMail` BIT(1)  NOT NULL DEFAULT 1;

CREATE TABLE `InsightShareMailTask` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `attempt` int(11) NOT NULL,
  `created` datetime DEFAULT NULL,
  `language` varchar(255) DEFAULT NULL,
  `sendTo` varchar(255) DEFAULT NULL,
  `sent` bit(1) NOT NULL,
  `share_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK9523204328EE95C6` (`share_id`),
  CONSTRAINT `FK9523204328EE95C6` FOREIGN KEY (`share_id`) REFERENCES `InsightShare` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8