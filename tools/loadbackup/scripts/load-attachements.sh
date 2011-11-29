echo 'copying attachments to local attachments'
PATH=$1  #something like ../../Website/trunk/beansight/main/attachments/
rm -r $PATH
cp -r ./backup_prod/data/attachments/ $PATH

