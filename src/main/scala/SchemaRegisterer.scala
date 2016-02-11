import com.fasterxml.jackson.databind.{SerializationFeature, ObjectMapper}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import org.apache.avro.Schema.Parser
import play.api.libs.json.{JsLookupResult, JsValue}
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.libs.ws.ning.NingWSClient
import scala.collection.mutable
import scala.concurrent.{Await, ExecutionContext, Future}
import ExecutionContext.Implicits.global
import scala.util.{Failure, Success}
import scala.concurrent.duration._

case class SchemaAvro(schema: String)

object SchemaRegisterer {

  val cache = new mutable.HashMap[Int,String]()

  def generateSchemas = {

    val mapper = new ObjectMapper()
    mapper.registerModule(DefaultScalaModule)
    mapper.configure(SerializationFeature.INDENT_OUTPUT, false)
    def generateSchemaString(s: String): String = mapper.writeValueAsString(SchemaAvro(s))
    //EventGenerator.genearateEvents
    val orderSchemaString: String =
      """
                             {"namespace": "kafka.events",
                                 "type": "record",
                                 "name": "order",
                                 "fields": [
                                     {"name": "id", "type": "int"},
                                     {"name": "externalOrderId",  "type": "int"},
                                     {"name": "amount", "type": "double"},
                                     {"name": "dropUserId", "type": "int"},
                                     {"name": "dropAddressId", "type": "int"},
                                     {"name": "updatedAt", "type": "long"}
                                 ]
                                }
      """
    val orderSchema = new Parser().parse(orderSchemaString).toString
    val shipmentSchemaString: String =
      """
                             {"namespace": "kafka.events",
                                 "type": "record",
                                 "name": "shipment",
                                 "fields": [
                                     {"name": "id", "type": "int"},
                                     {"name": "deliveryTrip",  "type": "int"},
                                     {"name": "status", "type": "int"},
                                     {"name": "state", "type": "string"},
                                     {"name": "updatedAt", "type": "long"}
                                 ]
                                }
      """
    val shipmentSchema = new Parser().parse(shipmentSchemaString).toString
    val orderSchemaId = SchemaRegisterer.registerSchema("order-value", generateSchemaString(orderSchema))
    orderSchemaId.onComplete {
      case Success(id) => println(s"orderSchemaID is : ${id}")
      case Failure(message) => println(s"couldn't register the schema for order : ${message.getMessage}")
    }
    val shipmentSchemaId = SchemaRegisterer.registerSchema("shipment-value", generateSchemaString(shipmentSchema))
    shipmentSchemaId.onComplete {
      case Success(id) => println(s"shipmentSchemaID is : ${id}")
      case Failure(message) => println(s"couldn't register the schema for shipment : ${message.getMessage}")
    }
  }

  val endpoint = "http://localhost:8081"
  val ws = NingWSClient()

  def registerSchema(topic: String, schema: String): Future[String] = {
    val request: WSRequest = ws.url(s"${endpoint}/subjects/${topic}/versions")
    val requestWithHeaders: WSRequest = request.withHeaders("Content-Type" -> "application/vnd.schemaregistry.v1+json")
    val responseFuture: Future[WSResponse] = requestWithHeaders.post[String](schema)
    val schemaId: Future[String] = responseFuture.map[String]((response: WSResponse) => {
      response.body
    })
    schemaId
  }

  def testCompatibility(topic: String, schema: String) = {
    //    curl -X POST -i -H "Content-Type: application/vnd.schemaregistry.v1+json" \
    //      --data '{"schema": "{\"type\": \"string\"}"}' \
    //    http://localhost:8081/compatibility/subjects/Kafka-value/versions/latest
  }

  def getSchema(topicId: Int): Option[String] = {

    cache.get(topicId) match {
      case Some(s) => Some(s)
      case None => {
        val responseFuture: Future[WSResponse] = ws.url(s"${endpoint}/schemas/ids/${topicId}").get()
        val response: WSResponse = Await.result(responseFuture, 5000 millis)
        val schema: Option[String] = if (response.status == 200) {Some((response.json \ "schema").as[String])} else {None}
        schema match{
          case Some(s) => {
            cache.put(topicId,s)
            Some(s)
          }
          case None => {
            println("couldn't get the schema")
            None
          }
        }

      }
    }
    //    val response = responseFuture.map{
    //      case response:WSResponse => {
    //        if(response.status == 200){
    //          Some((response.json \ "schema").as[String])
    //        }else{
    //          None
    //        }
    //      }
    //      case timeout => None
    //    }
  }

def getSchema(topicName: String, version: Int = -1) = {
  version match {
    case -1 =>
    //todo:fetch latest one if versionis not mentioned
    //curl -X GET -i http://localhost:8081/subjects/Kafka-value/versions/latest

    case _ =>
    //curl -X GET -i http://localhost:8081/subjects/Kafka-value/versions/1

  }
}

def checkSchema(topic: String, schema: String) = {
  //    curl -X POST -i -H "Content-Type: application/vnd.schemaregistry.v1+json" \
  //      --data '{"schema": "{\"type\": \"string\"}"}' \
  //    http://localhost:8081/subjects/Kafka-key
  }


}

