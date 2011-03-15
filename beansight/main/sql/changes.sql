-- Add to this file the database changes that need to be applied to the %playapps database
-- Reset this page after each release and after having created a SVN tag
--

CREATE TABLE `InvitedSubscribedNotification` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `notifiedUser_id` bigint(20) DEFAULT NULL,
  `subscribedUser_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK7B92E2A0A5F5DA66` (`notifiedUser_id`),
  KEY `FK7B92E2A0CA548964` (`subscribedUser_id`),
  CONSTRAINT `FK7B92E2A0CA548964` FOREIGN KEY (`subscribedUser_id`) REFERENCES `User` (`id`),
  CONSTRAINT `FK7B92E2A0A5F5DA66` FOREIGN KEY (`notifiedUser_id`) REFERENCES `User` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8
