package com.kainos.client

import java.util.{Collections, Properties}

import com.kainos.schema.TokenMessage

import scala.collection.JavaConverters._
import org.apache.kafka.clients.consumer.{ConsumerConfig, KafkaConsumer}
import io.confluent.kafka.serializers.{
  AbstractKafkaAvroSerDeConfig,
  KafkaAvroDeserializer,
  KafkaAvroDeserializerConfig
}

@SuppressWarnings(Array("org.wartremover.warts.NonUnitStatements"))
object Consumer extends App {

  private val topic: String = "dockertest"

  println("Consumer started!")

  private val props: Properties = new Properties()

  props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka:9092")
  props.put(ConsumerConfig.GROUP_ID_CONFIG, "ConsumerGroup1")
  props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true")
  props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000")
  props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "30000")
  props.put(
    ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
    classOf[org.apache.kafka.common.serialization.StringDeserializer].getName)
  props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
            classOf[KafkaAvroDeserializer].getName)
  props.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, "true")
  props.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG,
            "http://schemaregistry:8081")

  private val consumer: KafkaConsumer[String, TokenMessage] =
    new KafkaConsumer[String, TokenMessage](props)
  consumer.subscribe(Collections.singletonList(this.topic))

  while (true) {

    println(s"Consumer Polling! topics: ${consumer.listTopics()}")
    val records = consumer.poll(1000)
    println(s"Got ${records.count()} records")
    for (record <- records.asScala) {
      println(s"Received message: (${record.key()}, ${record
        .value()}) at offset ${record.offset()} in partition ${record.partition()}")
    }
  }

  println("Consumer finished!")
}
