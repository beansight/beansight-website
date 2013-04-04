#!/bin/bash

pid=`pgrep lighttpd`

if [ -z $pid  ]; then
	echo "oops lighttpd is not there trying to restart it ..."
	sudo
else
	echo "ok process java has pid $pid"
fi
