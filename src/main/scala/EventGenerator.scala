import java.io.{FileWriter, BufferedWriter, PrintWriter, File}
import java.util.{Calendar, Date}
import com.fasterxml.jackson.databind.{SerializationFeature, ObjectMapper}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import scala.util.Random

trait EventSuper

case class Event(schemaId: Int, topic: String, event: EventSuper)
//case class Event(schemaId: Int, topic: String, event:List[Field])
case class Order(id: Int, externalOrderId: Int, amount: Float, dropUserId: Int, dropAddressId: Int, updatedAt: Date) extends EventSuper

case class Shipment(id: Int, deliveryTrip: Int, status: Int, state: String, updatedAt: Date) extends EventSuper

case class WrongOrder(id: Long, externalOrderId: Int, dropUserId: Int, dropAddressId: Int, updatedAt: Date) extends EventSuper




object EventGenerator {
  def genearateEvents = {

    val random: Random = new Random
    val ext: (Int) => Int = { x: Int => (x * random.nextInt(10)) / 5 }
    val state = List("created", "reached", "pickedup", "delivered", "cancelled")
    val file: File = new File("events.txt")
    val bw = new BufferedWriter(new FileWriter(file))
    val mapper = new ObjectMapper()
    mapper.registerModule(DefaultScalaModule)

    val enablePrettyWriter = false
    enablePrettyWriter match {
      case true ⇒ mapper.configure(SerializationFeature.INDENT_OUTPUT, true)
      case false ⇒ mapper.configure(SerializationFeature.INDENT_OUTPUT, false)
    }

    (1 to 1000).foreach(x => {
      (x % 100) match {
        case 0 => {
          val wrongOrder = WrongOrder(x.toLong, ext(x), random.nextInt(50), random.nextInt(100), Calendar.getInstance().getTime())
          val wrongOrderEvent = Event(21, "order", wrongOrder)
          val wrongOrderJson: String = mapper.writeValueAsString(wrongOrderEvent)
          bw.write(wrongOrderJson)
          bw.newLine()
        }
        case _ => {
          (x % 2 == 0) match {
            case true => {
              val order = Order(x, ext(x), random.nextInt(400), random.nextInt(50), random.nextInt(100), Calendar.getInstance().getTime())
              val orderEvent = Event(21, "order", order)
              val orderJson: String = mapper.writeValueAsString(orderEvent)
              bw.write(orderJson)
              bw.newLine()
            }
            case false => {
              val shipment = Shipment(x, x + 50, random.nextInt(5), state(random.nextInt(4)), Calendar.getInstance().getTime())
              val shipmentEvent = Event(3, "shipment", shipment)
              val shipmentJson: String = mapper.writeValueAsString(shipmentEvent)
              bw.write(shipmentJson)
              bw.newLine()
            }
          }

        }
      }
      //Thread.sleep(5000)
    })
    bw.close()
  }

}


