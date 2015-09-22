package actors

import javax.inject.Inject
import com.google.inject.name.Named
import akka.actor.{ ActorRef, ActorSystem, Props, Actor }
import java.util.UUID
import org.joda.time.LocalDateTime
import play.api.Play.current
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import actors.PGActor._
import actors.StripeActor._
import actors.TwilioActor._
import actors.CRMActor._
import actors.AccountActor._
import actors.CommunicateActor._
import play.api.Logger
import akka.event.LoggingReceive



object WebSocketActor {

  def props(user: UUID, twilioActor: ActorRef, crmActor: ActorRef, commActor: ActorRef, accActor: ActorRef, stripesupActor: ActorRef, out: ActorRef) = Props(new WebSocketActor(user, twilioActor, crmActor, commActor, accActor, stripesupActor, out))


  case class Ins(from: String, typ: String, date: String, msg: String, name: String, driverphone: String, isDone: Boolean)
  object Ins{
    implicit val insReads: Reads[Ins] = (
      (JsPath \ "from").read[String] and
      (JsPath \ "typ").read[String] and
      (JsPath \ "date").read[String] and
      (JsPath \ "msg").read[String] and
      (JsPath \ "name").read[String] and
      (JsPath \ "driverphone").read[String] and
      (JsPath \ "isDone").read[Boolean]
    )(Ins.apply _)

    implicit val insWrites: Writes[Ins] = (
      (JsPath \ "from").write[String] and
      (JsPath \ "typ").write[String] and
      (JsPath \ "date").write[String] and
      (JsPath \ "msg").write[String] and
      (JsPath \ "name").write[String] and
      (JsPath \ "driverphone").write[String] and
      (JsPath \ "isDone").write[Boolean]
    )(unlift(Ins.unapply))
  }

}

class WebSocketActor(user: UUID, twilioActor: ActorRef, crmActor: ActorRef, commActor: ActorRef, accActor: ActorRef, stripesupActor: ActorRef, out: ActorRef) extends Actor {

  import WebSocketActor._

  override def preStart() {
    commActor ! Join(user, out)
  }
  override def postStop() {
    commActor ! UnJoin(user, out)
  }

  def receive = LoggingReceive {

    case msg: JsValue => Logger.info(s"websocket recieved some json: $msg")
      msg.validate[Ins] match {
        case error: JsError => play.Logger.error("couldn't parse msg in websocket event")//Notify
        case sucess: JsSuccess[Ins] =>  

         val s = sucess.getOrElse(Ins("from", "typ", "date", "msg", "name", "driverphone", false))

            if (s.typ == "REPLY") { 

              val time = new LocalDateTime()
              val created = time.toString()  
              //twilioActor ! SendSMS(s.from, s.msg, s.driverphone)
              crmActor ! SendMMSMsg(s.from, s.msg, s.driverphone)
              accActor !  Accounter(s.from, "REPLY", created, s.msg, s.name, s.driverphone, true)

           } else if (s.typ == "MARKATTENDED") {

              val time = new LocalDateTime()
              val created = time.toString()  
              accActor !  MarkCompleted(s.from, "MARKATTENDED", created, s.msg, s.name, s.driverphone, true)

            } else if (s.typ == "MARKTAKEN") {

              val time = new LocalDateTime()
              val created = time.toString()
              accActor !  MarkTaken(s.from, "MARKTAKEN", created, s.msg, s.name, s.driverphone, true)


             } else if (s.typ == "REFRESH") {

              accActor !  Refresh

             } else if (s.typ == "RECALL") {

              accActor !  Recall

             } else if (s.typ == "RETRIEVE") {

              //twilioActor ! GetSMSList(s.msg)

              crmActor ! GetCustMsgsbyMobile(s.from, s.date, s.msg, s.driverphone, s.name)


            } else if (s.typ == "BILLCUSTOMER") {

            val amt = java.lang.Integer.parseInt(s.msg)
              stripesupActor ! ChargeCustomer(s.from, amt.toInt) 

            } else if (s.typ == "REFUNDCUSTOMER") {

              stripesupActor ! RefundCustomer(s.msg)

            } else {
              Logger.info("unknown websocket message type")
            }
      }

  }
}
