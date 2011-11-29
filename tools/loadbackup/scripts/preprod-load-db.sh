echo 'copying db dump to preprod server'
scp -i ~/.ssh/preprod@beansight.com_rsa ./backup_prod/mysql-dump.gz play@92.243.10.157:./temp/mysql-dump.gz

echo 'drop preprod play database'
scp -i ~/.ssh/preprod@beansight.com_rsa ./scripts/init-preprod.sql play@92.243.10.157:./temp/init-preprod.sql
ssh -i ~/.ssh/preprod@beansight.com_rsa play@92.243.10.157 'mysql-console -D play < ./temp/init-preprod.sql'

echo 'importing dump to preprod database'
ssh -i ~/.ssh/preprod@beansight.com_rsa play@92.243.10.157 'rm ./temp/mysql-dump; gzip -d ./temp/mysql-dump.gz; mysql-console -D play < ./temp/mysql-dump'


