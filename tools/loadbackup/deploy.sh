BEANSIGHTCHANGESPATH=../../Website/trunk/beansight/main

echo 'removing tmp'
rm -r $BEANSIGHTCHANGESPATH/tmp

echo 'removing attachments'
rm -r $BEANSIGHTCHANGESPATH/attachments

echo 'removing data'
rm -r $BEANSIGHTCHANGESPATH/data

echo 'removing *log'
rm $BEANSIGHTCHANGESPATH/*.log

echo 'launching playapps deploy'
cd $BEANSIGHTCHANGESPATH
play playapps:deploy
