echo "Download a complete backup and refresh attachements and database. Could be very long. Are you sure? (Ctrl-C to stop)"
read -e CONFIRM
./scripts/download-backup.sh
./scripts/load-db.sh
./scripts/load-attachements.sh ../../Website/trunk/beansight/main/attachments/
echo 'done'
