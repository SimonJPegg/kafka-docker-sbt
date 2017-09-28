package com.kainos.consumer

import java.util.Properties
import java.util.concurrent.Future

import com.kainos.traits.Consumer
import org.apache.kafka.clients.consumer.{ConsumerConfig, KafkaConsumer}
import org.apache.kafka.clients.producer.RecordMetadata
import org.apache.kafka.common.serialization.StringDeserializer

import scala.collection.JavaConversions._

object StringConsumer extends Consumer[String, String] {

  val topics: List[String] = List("shakespeare_strings")

  def properties: Properties = {
    val props = new Properties
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka:9092")
    props.put(ConsumerConfig.GROUP_ID_CONFIG, "testgroup")
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
              classOf[StringDeserializer])
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
              classOf[StringDeserializer])
    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
    props
  }

  def create: KafkaConsumer[String, String] = {
    val consumer = new KafkaConsumer[String, String](properties)
    consumer.subscribe(topics)
    consumer.poll(0)
    consumer.beginningOffsets(consumer.assignment())
    consumer
  }

  def consume(
      consumer: KafkaConsumer[String, String],
      consumeFunction: (String, String) => Future[RecordMetadata]): Unit =
    try {
      while (true) {
        consumer.poll(100L).foreach { record =>
          consumeFunction(record.key(), record.value())
        }
      }
    } finally {
      consumer.close()
    }
}
