-- Add to this file the database changes that need to be applied to the %playapps database
-- Reset this page after each release and after having created a SVN tag
--

ALTER TABLE `Message` MODIFY COLUMN `content` LONGTEXT  CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL;

