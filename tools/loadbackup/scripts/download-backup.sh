
echo 'removing previous backup prod'
rm -r ./backup_prod

echo 'retrieving current backup from prod and copying to ./backup_prod'
scp -i ~/.ssh/prod@beansight.com  -r play@92.243.16.31:/backup/current ./backup_prod

echo 'decompress mysql dump'
gzip -d ./backup_prod/mysql-dump.gz
