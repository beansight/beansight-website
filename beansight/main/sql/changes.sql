-- Add to this file the database changes that need to be applied to the %playapps database
-- Reset this page after each release and after having created a SVN tag
--

ALTER TABLE `InsightActivity` ADD COLUMN `totalCount` BIGINT(20)  NOT NULL AFTER `totalCount`;
ALTER TABLE `InsightActivity` ADD COLUMN `newCommentCount` BIGINT(20)  NOT NULL AFTER `totalCount`;



UPDATE `User` SET `invitationsLeft` = `invitationsLeft` + 5;
