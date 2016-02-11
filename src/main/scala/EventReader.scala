import java.util.Properties
import java.util.logging.Logger
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import org.apache.avro.Schema
import org.apache.avro.Schema.Parser
import org.apache.avro.generic.GenericData.Record
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.producer.{ProducerRecord, KafkaProducer}


import scala.collection.immutable.HashMap
import scala.collection.mutable
import scala.io.Source

object EventReader {
  def sentToKafka ={
    val log = Logger.getLogger(getClass.getName)
    val props = new Properties()
    props.put("request.required.acks", "1")
    props.put("bootstrap.servers", "127.0.0.1:9092")
    props.put("schema.registry.url", "http://127.0.0.1:8081")
    props.put("key.serializer", "io.confluent.kafka.serializers.KafkaAvroSerializer")
    props.put("value.serializer", "io.confluent.kafka.serializers.KafkaAvroSerializer")

    val producer = new KafkaProducer[String, GenericRecord](props)

    val mapper = new ObjectMapper()
    mapper.registerModule(DefaultScalaModule)
    //mapper.configure(DeserializationFeature.FAIL_ON_UNRESOLVED_OBJECT_IDS, false)

    Source.fromFile("events.txt").getLines().foreach((line: String) => {
      val map = mapper.readValue(line, classOf[HashMap[String,Any]])
      val topic = map.get("topic").get.asInstanceOf[String]
      val schemaId = map.get("schemaId").get.asInstanceOf[Int]
      val event = map.get("event").get.asInstanceOf[HashMap[String,Any]]


      val schemaString: Option[String] = SchemaRegisterer.getSchema(schemaId)
      schemaString match {
        case Some(s) => {
          val schema: Schema = new Parser().parse(s)
          val record: Record = new Record(schema)

          event.foreach((x: (String, Any)) => {
            record.put(x._1,x._2)
          })

          val producerRecord: ProducerRecord[String, GenericRecord] = new ProducerRecord[String, GenericRecord](topic,record)
          try{
            producer.send(producerRecord)
            log.info("success")
          }catch{
            case e => log.warning(e.toString)
          }

        }
        case None => {
          log.warning(s"Couldn't find the schema for the id:${schemaId}")
        }
      }

    })
  }

}
