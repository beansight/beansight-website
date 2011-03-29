-- Add to this file the database changes that need to be applied to the %playapps database
-- Reset this page after each release and after having created a SVN tag
--

ALTER TABLE `InsightActivity` ADD COLUMN `totalCount` BIGINT(20)  NOT NULL;
ALTER TABLE `InsightActivity` ADD COLUMN `newCommentCount` BIGINT(20)  NOT NULL;



UPDATE `User` SET `invitationsLeft` = `invitationsLeft` + 5;
