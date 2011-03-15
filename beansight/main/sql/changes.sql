-- Add to this file the database changes that need to be applied to the %playapps database
-- Reset this page after each release and after having created a SVN tag
--

UPDATE `User` SET `invitationsLeft` = `invitationsLeft` + 5 WHERE `crdate` < '2011-03-16 12:00:00'