#!/bin/bash
#echo 'removing previous backup prod'
#/bin/rm -r ./backup_prod
#echo 'retrieving current backup from prod and copying to ./backup_prod'
#/usr/bin/scp -i ~/.ssh/prod@beansight.com  -r play@92.243.16.31:/backup/current ./backup_prod

#echo 'copying attachments to local attachments'
#PATH=../../beansight/main/attachments/
#/bin/rm -r $PATH
#/bin/cp -r ./backup_prod/data/attachments/ $PATH

echo 'decompress mysql dump'
/bin/gzip -d ./backup_prod/mysql-dump

echo 'drop local beansight database'
/usr/bin/mysql -u root --password=play < scripts/init.sql

echo 'loading mysql dump file'
/usr/bin/mysql -u root --password=play -D beansight < ./backup_prod/mysql-dump
