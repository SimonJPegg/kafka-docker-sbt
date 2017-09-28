package com.kainos.consumer

import java.util.Properties

import com.kainos.traits._
import com.kainos.schema.{ShakespeareKey, ShakespeareValue}
import io.confluent.kafka.serializers.KafkaAvroDeserializer
import org.apache.kafka.clients.consumer.{ConsumerConfig, KafkaConsumer}

import scala.collection.JavaConversions._

object AvroConsumer extends Consumer[ShakespeareKey, ShakespeareValue] {

  def main(args: Array[String]): Unit = {
    AvroConsumer.consume(AvroConsumer.create)
  }

  val topics: List[String] = List("shakespeare_avro")

  def consume(consumer: KafkaConsumer[ShakespeareKey, ShakespeareValue]): Unit =
    try {
      while (true) {
        consumer.poll(100).foreach { record =>
          println(
            s"Got (${record.key().getWork}:${record.key().getYear}) : (${record
              .value()
              .getLine}: ${record.value().getLine})")
        }
      }
    } finally {
      consumer.close()
    }

  def create: KafkaConsumer[ShakespeareKey, ShakespeareValue] = {
    val consumer =
      new KafkaConsumer[ShakespeareKey, ShakespeareValue](properties)
    consumer.subscribe(topics)
    consumer.poll(0)
    consumer.beginningOffsets(consumer.assignment())
    consumer
  }

  def properties: Properties = {
    val props = new Properties
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka:9092")
    props.put(ConsumerConfig.GROUP_ID_CONFIG, "testgroup")
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
              classOf[KafkaAvroDeserializer])
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
              classOf[KafkaAvroDeserializer])
    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
    props.put("specific.avro.reader", "true")
    props.put("schema.registry.url", "http://schemaregistry:8081")
    props
  }
}
