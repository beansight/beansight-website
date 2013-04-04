#!/bin/bash

pid=`pgrep java`

if [ -z $pid  ]; then
	echo "oops java is not there trying to restart it ..."
	rm /home/admin/beansight-website/beansight/main/server.pid
	play start /home/admin/beansight-website/beansight/main/
else
	echo "ok process java has pid $pid"
fi
