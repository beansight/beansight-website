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