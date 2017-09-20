package com.kainos.client

import java.util.Properties
import java.util.concurrent.TimeUnit

import com.kainos.schema.TokenMessage
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}
import io.confluent.kafka.serializers.{AbstractKafkaAvroSerDeConfig, KafkaAvroSerializer}
import org.apache.kafka.common.serialization.StringSerializer
import scala.util.Random

@SuppressWarnings(Array("org.wartremover.warts.NonUnitStatements"))
object Producer extends App {

  println("Producer Started")

  private val topic: String = "dockertest"

  private val schemaRegistryURL = "http://schemaregistry:8081"

  private val props: Properties = new Properties()
  props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka:9092")
  props.put(ProducerConfig.CLIENT_ID_CONFIG, "ScalaProducerExample")
  props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
            classOf[StringSerializer])
  props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
            classOf[KafkaAvroSerializer])
  props.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG,
            schemaRegistryURL)

  private val producer: KafkaProducer[String, TokenMessage] =
    new KafkaProducer[String, TokenMessage](props)

  var key = 0

  while (true) {
    key += 1

    val lines: Seq[String] = {
      List.fill(new Random().nextInt(1000))(generatePAN)
    }

    lines.foreach { line =>
      val message = new TokenMessage(
        line,
        scala.io.Source
          .fromURL(s"http://tokenserver:8888/token/$line")
          .mkString
      )
      println(s"Sending Message $message")
      val javaFuture = producer.send(
        new ProducerRecord[String, TokenMessage](topic, key.toString, message)
      )
      producer.flush()
      val result = javaFuture.get(5, TimeUnit.SECONDS)
      println(s"Sent message to ${result.topic()} with offset ${result
        .offset()} in partition ${result.partition()}")
    }
  }

  println("Producer Completed")

  private def generatePAN: String = {
    val random = new Random()
    List.fill(16)(random.nextInt(9)).mkString
  }
}
