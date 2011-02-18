-- Add to this file the database changes that need to be applied to the %playapps database
-- Reset this page after each release and after having created a SVN tag
--

ALTER TABLE `User`
ADD COLUMN `isPromocodeValidated` bit(1) NOT NULL;