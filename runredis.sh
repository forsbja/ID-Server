#!/bin/bash
cd ./docker
if [ "$1" = "start" ];then
    docker-compose up -d
elif [ "$1" = "stop" ];then
    docker-compose down
else
    "Invalid argument!"
fi
cd ..