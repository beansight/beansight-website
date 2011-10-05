-- Add to this file the database changes that need to be applied to the %playapps database
-- Reset this page after each release and after having created a SVN tag
--

-- this index should help to have better perf when querying the Vote using creationDate constraints
CREATE INDEX VOTE_INSIGHTID_CREATIONDATE_IDX on Vote (insight_id, creationDate);