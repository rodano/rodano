#!/usr/bin/env bash

#retrieve parameters
ACTION=$1
CONTAINER_NAME=$2

#check parameters
if [[ -z $ACTION ]]
then
	echo "Usage: {start|stop|restart}"
	exit 1
fi

#fail if a variable is referenced before being set or if a command fails
set -u -e -x

#retrieve environment variables
BACKEND_HOST=${BACKEND_HOST:-"localhost"}
BACKEND_PORT=${BACKEND_PORT:-8080}

function start {
	running=$(docker ps --filter "name=$CONTAINER_NAME" --filter "status=running" -q | wc -l)

	if [ "$running" -eq 0 ];
	then
		echo "Starting application..."
		docker start $CONTAINER_NAME

		#wait for start, using is running script (max 60 seconds)
		time=0
		success=0
		while [[ $time -lt 61 ]] && [[ $success -eq 0 ]]; do
			if curl --silent "http://$BACKEND_HOST:$BACKEND_PORT/administration/is-online" | jq -r ".message" | grep --ignore-case --quiet "up";
			then
				success=1
			else
				sleep 1
			fi
			time=$((time + 1))
		done

		#return error code in case of failure
		if [[ $success -eq 0 ]];
		then
			echo "Unable to start application"
			return 1
		else
			echo "Application started successfully in $time seconds."
			return 0
		fi
	else
		echo "Application is already running."
		return 0
	fi
}

function stop {
	running=$(docker ps --filter "name=$CONTAINER_NAME" --filter "status=running" -q | wc -l)

	if [ "$running" -ne 0 ];
	then
		echo "Stopping application..."

		#keep the current time to be able to check the journal
		date=$(date +"%Y-%m-%dT%H:%M:%S")

		docker stop $CONTAINER_NAME

		#wait for stop, using is running script (max 10 seconds)
		time=0
		success=0
		while [[ $time -lt 11 ]] && [[ $success -eq 0 ]]; do
			if docker logs $CONTAINER_NAME --since="$date" | grep --ignore-case --quiet "stopped";
			then
				success=1
			else
				sleep 1
			fi
			time=$((time + 1))
		done

		#return error code in case of failure
		if [[ $success -eq 0 ]];
		then
			echo "Unable to stop application"
			return 1
		else
			echo "Application stopped successfully in $time seconds."
			return 0
		fi
	else
		echo "Application is not running."
		return 0
	fi
}

case "$ACTION" in
	#start application
	start)
		start
		exit 0
		;;
	#stop application
	stop)
		stop
		exit 0
		;;
	#restart application
	restart)
		stop
		start
		echo "Application restarted successfully."
		exit 0
		;;
esac
