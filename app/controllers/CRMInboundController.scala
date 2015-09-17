package controllers

import javax.inject.Inject
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.concurrent.Akka
import akka.actor.{ ActorRef, ActorSystem, Props, Actor }
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._
import scala.util.{ Success, Failure }
import play.api.Logger
import play.api.mvc._
import play.api.Play.current
import scala.language.postfixOps
import org.joda.time.LocalDateTime
import org.joda.time.LocalTime
import actors.PGActor._
import com.google.inject.name.Named


//@Singleton
class CRMInboundController @Inject() (@Named("postgresql-actor") pgActor: ActorRef)  
																extends Controller {

//https://blooming-sea-8888.herokuapp.com/crmmessages

// Recieves http request(messages) from twilio. Throwsaways messages with no content and forwards the rest 
// to the incomming messages actor.

//	case class Msg(custID: String, message: String, keyword: String, mobileNum: String, optInStatus: String, 
//	timeStamp: String, subacct: String, custName: String, msgID: String, subacct_name: String, mms: String)

  def messages = Action(parse.urlFormEncoded) { implicit request =>
	  val mid = request.body("msgid").head//.toString
	  val from = request.body("mobilenum").head//.toString
	  val msg = request.body("message").head//.toString
	  val time = LocalDateTime.now()
	  val date = time.toString()

	pgActor !  new Message(mid, from, date, msg)
	
	Logger.info(s"inbound controller recieved message ${from}")

	Ok
  }


}