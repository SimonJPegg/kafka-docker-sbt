package com.kainos.traits


import java.util.Properties

import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer


trait Client[K,V] {
  def properties : Properties
}

trait Consumer[K,V] extends Client[K,V]{
  def create: KafkaConsumer[K,V]
}

trait Producer[K,V] extends Client[K,V]{
  def create: KafkaProducer[K,V]
}
