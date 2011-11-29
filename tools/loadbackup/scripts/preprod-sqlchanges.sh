
SQLCHANGESPATH=../../Website/trunk/beansight/main/sql

echo 'copying changes.sql to preprod server'
scp -i ~/.ssh/preprod@beansight.com_rsa $SQLCHANGESPATH/changes.sql play@92.243.10.157:./temp/changes.sql

echo 'applying changes.sql to preprod database'
ssh -i ~/.ssh/preprod@beansight.com_rsa play@92.243.10.157 'mysql-console -D beansight < ./temp/changes.sql'


