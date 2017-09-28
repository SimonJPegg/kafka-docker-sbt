#!/usr/bin/env bash
/app/wait-for-it.sh zookeeper:2181 -t 60 -- echo "Zookeeper started"
/app/wait-for-it.sh kafka:9092 -t 60 -- echo "Kafka started"
/app/wait-for-it.sh schemaregistry:8081 -t 60 -- echo "schemaregistry started"

sleep 10