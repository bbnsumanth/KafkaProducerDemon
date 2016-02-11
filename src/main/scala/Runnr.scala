/**
  * start zookeeper
  * ./bin/zookeeper-server-start ./etc/kafka/zookeeper.properties &
  * start kafka broker
  * ./bin/kafka-server-start ./etc/kafka/server.properties &
  * start schema registry
  * ./bin/schema-registry-start ./etc/schema-registry/schema-registry.properties &
  * start avro-console-consumer
  * ./bin/kafka-avro-console-consumer --topic order --zookeeper localhost:2181 --from-beginning
  * start consumer console-consumer
  * ./bin/kafka-console-consumer --topic order --zookeeper localhost:2181 --from-beginning
  *
  */

object Runnr extends App {
  //SchemaRegisterer.generateSchemas
  //EventGenerator.genearateEvents
  EventReader.sentToKafka

}

