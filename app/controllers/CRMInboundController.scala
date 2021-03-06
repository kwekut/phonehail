package controllers

import javax.inject.Inject
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.concurrent.Akka
import akka.actor.{ ActorRef, ActorSystem, Props, Actor }
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._
import scala.util.{ Success, Failure }
import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.Logger
import play.api.mvc._
import play.api.Play.current
import scala.language.postfixOps
import org.joda.time.LocalDateTime
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import org.joda.time.DateTimeZone
import org.joda.time.DateTime
import actors.PGActor._
import com.google.inject.name.Named


//@Singleton
class CRMInboundController @Inject() (@Named("postgresql-actor") pgActor: ActorRef)  
																extends Controller {

//https://blooming-sea-8888.herokuapp.com/crmmessages
//http://68e9d71e.ngrok.io/crmmessages

// Recieves http request(messages) from twilio. Throwsaways messages with no content and forwards the rest 
// to the incomming messages actor.

//	case class Msg(custID: String, message: String, keyword: String, mobileNum: String, optInStatus: String, 
//	timeStamp: String, subacct: String, custName: String, msgID: String, subacct_name: String, mms: String)

  def messages = Action(parse.multipartFormData) { implicit request =>
		Logger.info("crm parse called")
  	  val mid = request.body.dataParts.get("msgID") map (x => x.mkString)
	  val from = request.body.dataParts.get("mobileNum") map (x => x.mkString)
	  val msg = request.body.dataParts.get("message") map (x => x.mkString)

	  val LA: DateTimeZone = DateTimeZone.forID("America/Los_Angeles")
  	  val dtf: DateTimeFormatter = DateTimeFormat.forPattern("yyyy-mm-dd HH:MM:SS Z")
	  val created = dtf.withZone(LA).parseDateTime(DateTime.now.toString("yyyy-mm-dd HH:MM:SS Z")).toString("yyyy-mm-dd HH:MM:SS Z")

	pgActor !  new Message(mid.getOrElse(""), from.getOrElse(""), created, msg.getOrElse(""))
	
	// Logger.info(from.getOrElse("nothing"))
	// Logger.info(mid.getOrElse("nothing"))
	// Logger.info(msg.getOrElse("nothing"))

	Ok
  }

 //  def messages = Action(parse.json) { implicit request =>

 //  	  val mid = (request.body \ "msgid").asOpt[String]
	//   val from = (request.body \ "mobilenum").asOpt[String]
	//   val msg = (request.body \ "message").asOpt[String]
	//   val time = LocalDateTime.now()
	//   val date = time.toString()

	// pgActor !  new Message(mid.getOrElse(""), from.getOrElse(""), date, msg.getOrElse(""))
	
	// Logger.info(s"inbound controller recieved message ${from}")

	// Ok
 //  }


}