-- Add to this file the database changes that need to be applied to the %playapps database
-- Reset this page after each release and after having created a SVN tag
--

ALTER TABLE `User` ADD COLUMN `isDangerous` BIT(1)  NOT NULL;

CREATE TABLE `ApiAccessTokenStore` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `accessToken` varchar(255) DEFAULT NULL,
  `crdate` datetime DEFAULT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK99DAD90647140EFE` (`user_id`),
  KEY `API_USER_TOKEN_IDX` (`accessToken`),
  CONSTRAINT `FK99DAD90647140EFE` FOREIGN KEY (`user_id`) REFERENCES `User` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;