package com.kainos.client

import java.util.Properties

import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}

import scala.collection.immutable.Seq
import scala.util.Random

@SuppressWarnings(Array("org.wartremover.warts.NonUnitStatements"))
object Producer extends App {

  println("Producer Started")

  private val topic: String = "dockertest"

  private val props: Properties = new Properties()
  props.put("bootstrap.servers", "kafka:9092")
  props.put("client.id", "ScalaProducerExample")
  props.put("key.serializer",
            "org.apache.kafka.common.serialization.StringSerializer")
  props.put("value.serializer",
            "org.apache.kafka.common.serialization.StringSerializer")

  private val producer: KafkaProducer[String, String] =
    new KafkaProducer[String, String](props)

  private val lines: Seq[String] =
    List.fill(new Random().nextInt(1000))(generatePAN)

  lines.map { line =>
    val message = (
      line,
      scala.io.Source.fromURL(s"http://tokenserver:8888/token/$line").mkString
    )
    println(s"Sending Message $message")
    producer.send(
      new ProducerRecord[String, String](topic, message._1, message._2)
    )
  }

  println("Producer Completed")

  private def generatePAN: String = {
    val random = new Random()
    List.fill(16)(random.nextInt(9)).mkString
  }
}
