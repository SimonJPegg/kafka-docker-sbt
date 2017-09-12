package com.kainos.client

import java.util.{Collections, Properties}
import scala.collection.JavaConverters._
import org.apache.kafka.clients.consumer.{ConsumerConfig, KafkaConsumer}

@SuppressWarnings(Array("org.wartremover.warts.NonUnitStatements"))
object Consumer extends App {

  private val topic: String = "dockertest"

  private val props: Properties = new Properties()
  props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka:9092")
  props.put(ConsumerConfig.GROUP_ID_CONFIG, "ConsumerGroup1")
  props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true")
  props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000")
  props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "30000")
  props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
            "org.apache.kafka.common.serialization.StringDeserializer")
  props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
            "org.apache.kafka.common.serialization.StringDeserializer")

  private val consumer: KafkaConsumer[String, String] =
    new KafkaConsumer[String, String](props)
  consumer.subscribe(Collections.singletonList(this.topic))

  while (true) {
    val records = consumer.poll(1000)
    for (record <- records.asScala) {
      println(s"Received message: (${record.key()}, ${record
        .value()}) at offset ${record.offset()} in partition ${record.partition()}")
    }
  }
}
