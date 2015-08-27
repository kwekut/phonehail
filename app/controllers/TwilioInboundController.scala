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
class TwilioInboundController @Inject() (@Named("postgresql-actor") pgActor: ActorRef)  
																extends Controller {

// Recieves http request(messages) from twilio. Throwsaways messages with no content and forwards the rest 
// to the incomming messages actor.
  def messages = Action(parse.urlFormEncoded) { implicit request =>
	  val mid = request.body("MessageSid").head//.toString
	  val from = request.body("From").head//.toString
	  val to = request.body("To").head//.toString
	  val msg = request.body("Body").head//.toString
	  val time = LocalDateTime.now()
	  val date = time.toString()

	pgActor !  new Message(mid, from, to, date, msg)
	
	Logger.info(s"inbound controller recieved message ${from}")

	Ok
  }

    //case class Message(mid: String, from: String, to: String, date: String, msg: String)	
}