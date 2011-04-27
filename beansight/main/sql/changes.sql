-- Add to this file the database changes that need to be applied to the %playapps database
-- Reset this page after each release and after having created a SVN tag
--

CREATE TABLE `User_Topic` (
  `User_id` bigint(20) NOT NULL,
  `followedTopics_id` bigint(20) NOT NULL,
  PRIMARY KEY (`User_id`,`followedTopics_id`),
  KEY `FKE86DC3BBFCD8491` (`followedTopics_id`),
  KEY `FKE86DC3BB47140EFE` (`User_id`),
  CONSTRAINT `FKE86DC3BB47140EFE` FOREIGN KEY (`User_id`) REFERENCES `User` (`id`),
  CONSTRAINT `FKE86DC3BBFCD8491` FOREIGN KEY (`followedTopics_id`) REFERENCES `Topic` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

