-- Add to this file the database changes that need to be applied to the %playapps database
-- Reset this page after each release and after having created a SVN tag
--

ALTER TABLE `FeaturedTopic`
    ADD COLUMN `language_id` bigint(20) DEFAULT NULL,
    ADD KEY `FKF0B39581F53795DE` (`language_id`),
    ADD CONSTRAINT `FKF0B39581F53795DE` FOREIGN KEY (`language_id`) REFERENCES `Language` (`id`);