import com.fasterxml.jackson.databind.{SerializationFeature, ObjectMapper}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import org.apache.avro.Schema

import org.apache.avro.Schema.Parser
case class SchemaAvro(schema:String)
val orderSchemaString: String ="""
                             {"namespace": "kafka.events",
                                 "type": "record",
                                 "name": "order",
                                 "fields": [
                                     {"name": "id", "type": "int"},
                                     {"name": "externalOrderId",  "type": "int"},
                                     {"name": "amount", "type": "float"},
                                     {"name": "dropUserId", "type": "int"},
                                     {"name": "dropAddressId", "type": "int"},
                                     {"name": "updatedAt", "type": "long"}
                                 ]
                                }
                               """


val mapper = new ObjectMapper()
mapper.registerModule(DefaultScalaModule)
mapper.configure(SerializationFeature.INDENT_OUTPUT, false)
def generateSchemaString(s:String): String = mapper.writeValueAsString(SchemaAvro(s))

val parser: Parser = new Schema.Parser()
val schema = parser.parse((orderSchemaString)).toString
generateSchemaString(schema)

