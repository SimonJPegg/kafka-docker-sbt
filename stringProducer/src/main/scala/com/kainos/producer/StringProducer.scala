package com.kainos.producer

import java.io.{BufferedReader, File, FileReader}
import java.nio.file.Paths
import java.util.Properties

import com.kainos.traits.Producer
import org.apache.kafka.clients.producer.{
  KafkaProducer,
  ProducerConfig,
  ProducerRecord
}
import org.apache.kafka.common.serialization.StringSerializer

object StringProducer extends Producer[String, String] {

  val topic = "shakespeare_strings"

  def main(args: Array[String]): Unit = {
    val producer = create

    val file = new File(Paths.get("/data/shakespeare").toString)
    if (file.isDirectory) {
      file.listFiles().foreach { sendFile(_, producer) }
    } else {
      sendFile(file, producer)
    }
  }

  def sendFile(file: File, producer: KafkaProducer[String, String]): Unit = {
    val key = file.getName.split("\\.")(0)
    val reader = new BufferedReader(new FileReader(file))

    Stream.continually(reader.readLine()).takeWhile(_ != null).foreach { line =>
      val record = new ProducerRecord[String, String](topic, key, line)
      producer.send(record)
    }
  }

  def create: KafkaProducer[String, String] =
    new KafkaProducer[String, String](properties)

  def properties: Properties = {
    val properties = new Properties()
    properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka:9092")
    properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                   classOf[StringSerializer])
    properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                   classOf[StringSerializer])
    properties
  }
}
