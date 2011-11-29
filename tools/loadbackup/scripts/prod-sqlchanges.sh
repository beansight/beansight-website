
SQLCHANGESPATH=../../Website/trunk/beansight/main/sql

echo 'copying changes.sql to PROD server'
scp -i ~/.ssh/prod@beansight.com $SQLCHANGESPATH/changes.sql play@92.243.16.31:./temp/changes.sql

echo 'WARNING: are you sure you want to apply changes.sql on the PROD database ? (CTRL-C to abort)'
read -e SURE

echo 'applying changes.sql to PROD database'
ssh -i ~/.ssh/prod@beansight.com play@92.243.16.31 'mysql-console -D beansight < ./temp/changes.sql'

