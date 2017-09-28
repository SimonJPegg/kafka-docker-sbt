package com.kainos.converter

import com.kainos.consumer.StringConsumer
import com.kainos.producer.AvroProducer

object StringConsumerAvroProducer {

  def main(args: Array[String]): Unit = {
    val producer = AvroProducer.create
    StringConsumer.consume(StringConsumer.create,
                           AvroProducer.produceFunction(AvroProducer.create))
  }

}
