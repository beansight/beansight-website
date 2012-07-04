
echo 'removing previous local db backup'
rm -r ./backup_prod
mkdir backup_prod

echo 'retrieving current db backup from prod and copying to ./backup_prod'
#scp -i ~/.ssh/prod@beansight.com  -r play@92.243.16.31:/backup/current/mysql-dump.gz ./backup_prod/mysql-dump.gz
scp admin@92.243.4.73:/home/admin/backup/hourly/dump.sql.gz ./backup_prod/mysql-dump.gz
