package com.kainos.producer

import java.util.Properties
import java.util.concurrent.Future
import java.util.regex.Pattern

import com.kainos.schema.{ShakespeareKey, ShakespeareValue}
import com.kainos.traits.Producer
import io.confluent.kafka.serializers.KafkaAvroSerializer
import org.apache.kafka.clients.producer.{
  KafkaProducer,
  ProducerConfig,
  ProducerRecord,
  RecordMetadata
}

object AvroProducer extends Producer[Any, Any] {

  val topics = List("shakespeare_avro")

  private val pattern = Pattern.compile("^\\s*(\\d*)\\s*(.*)$")

  def properties: Properties = {
    val prodProps = new Properties
    prodProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka:9092")
    prodProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                  classOf[KafkaAvroSerializer])
    prodProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                  classOf[KafkaAvroSerializer])
    prodProps.put("schema.registry.url", "http://schemaregistry:8081")
    prodProps
  }
  def create: KafkaProducer[Any, Any] = new KafkaProducer[Any, Any](properties)

  def produceFunction(producer: KafkaProducer[Any, Any])
    : (String, String) => Future[RecordMetadata] =
    (keyString: String, valueString: String) => {
      producer.send(
        new ProducerRecord[Any, Any](topics.head,
                                     getKey(keyString),
                                     getValue(valueString)))
    }

  def getKey(key: String): ShakespeareKey = key match {
    case "Hamlet"             => new ShakespeareKey(key, 1600)
    case "Julius Caesar"      => new ShakespeareKey(key, 1599)
    case "Macbeth"            => new ShakespeareKey(key, 1605)
    case "Merchant of Venice" => new ShakespeareKey(key, 1596)
    case "Othello"            => new ShakespeareKey(key, 1604)
    case "Romeo and Juliet"   => new ShakespeareKey(key, 1594)
    case s: String =>
      throw new RuntimeException(s"Could not find year written for $s")
  }

  def getValue(value: String): ShakespeareValue = {
    val matcher = pattern.matcher(value)
    if (matcher.matches()) {
      new ShakespeareValue(matcher.group(1).toInt, matcher.group(2))
    } else {
      null
    }

  }
}
