package com.kainos.client

import java.util.{Properties, UUID}

import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}

object Producer extends App {

  println("Client Started")

  val topic = "dockertest"

  val props = new Properties()
  props.put("bootstrap.servers", "kafka:9092")
  props.put("client.id", "ScalaProducerExample")
  props.put("key.serializer",
            "org.apache.kafka.common.serialization.StringSerializer")
  props.put("value.serializer",
            "org.apache.kafka.common.serialization.StringSerializer")

  val producer = new KafkaProducer[String, String](props)

  val lines = List("1234567890123456", "0987654321098765")

  lines.map { line =>
    val result =
      scala.io.Source.fromURL(s"http://tokenserver:8888/token/$line").mkString
    println(s"Got $result from server, generating kafka message")
    producer.send(
      new ProducerRecord[String, String](topic,
                                         UUID.randomUUID().toString,
                                         result)
    )
  }

  println("Client Completed")
}
