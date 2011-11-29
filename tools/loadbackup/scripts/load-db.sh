echo 'decompress mysql dump'
gzip -d ./backup_prod/mysql-dump.gz

echo "Enter your mysql root password:"
read -e PASSWORD

MYSQL=$1 # should be something like mysql

echo 'drop local beansight database'
$MYSQL -u root --password=$PASSWORD < scripts/init.sql

echo 'loading mysql dump file'
$MYSQL -u root --password=$PASSWORD -D beansight < ./backup_prod/mysql-dump
