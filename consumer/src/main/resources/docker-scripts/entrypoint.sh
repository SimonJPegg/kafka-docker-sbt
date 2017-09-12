#!/bin/bash

set -e
set -x

jar_name=$1
version=$2
debug_port=$3

source /app/start-kafka-and-zk.sh

if [ -n "$debug_port" ];
    then java -jar -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address="$debug_port" /app/"$jar_name"-assembly-"$version".jar;
else
    java -jar /app/"$jar_name"-assembly-"$version".jar;
fi