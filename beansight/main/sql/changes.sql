-- Add to this file the database changes that need to be applied to the %playapps database
-- Reset this page after each release and after having created a SVN tag
--

-- add the message language:
ALTER TABLE MessageMailTask ADD language VARCHAR(255) AFTER sendTo;
ALTER TABLE FollowNotificationTask ADD language VARCHAR(255) AFTER sendTo;
ALTER TABLE ContactMailTask ADD language VARCHAR(255) AFTER sendTo;
ALTER TABLE InvitationMailTask ADD language VARCHAR(255) AFTER sendTo;
