#!/usr/bin/env bash
/app/wait-for-it.sh zookeeper:2181 -t 60 -- echo "Zookeeper started"
/app/wait-for-it.sh kafka:9092 -t 60 -- echo "Kafka started"
/app/wait-for-it.sh tokenserver:8888 -t 60 -- echo "Tokenserver started"

sleep 10