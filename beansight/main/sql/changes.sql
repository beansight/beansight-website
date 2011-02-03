-- Add to this file the database changes that need to be applied to the %playapps database
-- Reset this page after each release and after having created a SVN tag
--

-- add the message language:
ALTER TABLE MessageMailTask ADD language VARCHAR(255) AFTER sendTo;
ALTER TABLE FollowNotificationTask ADD language VARCHAR(255) AFTER sendTo;
ALTER TABLE ContactMailTask ADD language VARCHAR(255) AFTER sendTo;
ALTER TABLE InvitationMailTask ADD language VARCHAR(255) AFTER sendTo;

CREATE TABLE `forgotpassword` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `code` varchar(255) DEFAULT NULL,
  `crDate` datetime DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FORGOT_PWD_CODE_IDX` (`code`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;