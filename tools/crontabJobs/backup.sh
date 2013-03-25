#!/bin/bash

destDir=~/backup 
dropboxScript=~/beansight-website/tools/dropbox-shell/Dropbox-Uploader-master

echo "using destDir=$destDir"
echo "using dropboxScript=$dropboxScript"

echo "creating backup of beansight db"
mysqldump -u root -pplay beansight | /bin/gzip > $destDir/dump.sql.gz

echo "creating backup of beansight app and more important: attachments"
tar -czf $destDir/beansight.tar.gz ~/beansight-website/beansight

echo "copy db backup to dropbox"
$dropboxScript/dropbox_uploader.sh upload $destDir/dump.sql.gz

echo "copy beansight backup to dropbox"

echo "first splitting beansight.tar.gz in 6 files ... "
rm -rf "$destDir/beansight_split"
mkdir "$destDir/beansight_split"

split -n 6 "$destDir/beansight.tar.gz" "$destDir/beansight_split/beansight.tar.gz."


for i in "$destDir"/beansight_split/*
do
	echo "uploading: $i"
	$dropboxScript/dropbox_uploader.sh upload $i
done;

#$dropboxScript/dropbox_uploader.sh upload $destDir/beansight.tar.gz beansight.tar.gz

