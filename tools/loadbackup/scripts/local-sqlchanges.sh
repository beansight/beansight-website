echo "Enter your mysql root password:"
read -e PASSWORD

SQLCHANGESPATH=../../Website/trunk/beansight/main/sql
MYSQL=$1 # should be something like mysql

echo 'load changes.sql to local database'
$MYSQL -u root --password=$PASSWORD -D beansight < $SQLCHANGESPATH/changes.sql
