package actors

import akka.actor._
import play.api.Play.current
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import scala.concurrent.Future
import play.api.Logger
import akka.event.LoggingReceive
import actors.PGActor._
import actors.TwilioActor._
import actors.AccountActor._
import javax.inject._
import com.google.inject.name.Named


object InBoundActor{
}

//Recieves messages from twilio via inbox and sends it to the websocket actor outs.
class InBoundActor @Inject() ( @Named("twilio-actor") twilioActor: ActorRef,
					 			@Named("account-actor") accActor: ActorRef ) extends Actor {

	import InBoundActor._

  def receive = LoggingReceive {
						
	case StartUser(from, date, msg, fullname, email, phone, address,  hasstripe, preferences) =>	
										accActor ! Notifier(from, "NOTIFICATION", date, "initiated start request", fullname, "driverphone", true)
										twilioActor ! SendSMS(from, "will get to you in a moment", "none")

	case StartNone(from, date, msg, fullname, email, phone, address,  hasstripe, preferences) =>
		twilioActor ! SendSMS(from, "please register for our service @ www.getdrivernow.com", "none")

	case StartError(from, date, msg, fullname, email, phone, address,  hasstripe, preferences) =>
	 accActor ! Notifier(from, "NOTIFICATION", date, "initiated start request", "NoName", "driverphone", true)
	 twilioActor ! SendSMS(from, "please register for our service @ www.getdrivernow.com", "none")

	case GenUser(from, date, msg, fullname, email, phone, address,  hasstripe, preferences) => 
										accActor ! Accounter(from, "INCOMMING", date, msg, fullname, "driverphone", false)
																
	case GenNone(from, date, msg, fullname, email, phone, address,  hasstripe, preferences) =>
	 twilioActor ! SendSMS(from, "please register for our service @ www.getdrivernow.com", "none")

	case GenError(from, date, msg, fullname, email, phone, address,  hasstripe, preferences) => 
	 accActor ! Accounter(from, "INCOMMING", date, msg, "NoName", "driverphone", false)

	case OutOfOffice(from, date, msg, fullname, email, phone, address,  hasstripe, preferences) =>
	 twilioActor ! SendSMS(from, "We provide services between the the hrs of 7pm - 8am , please see our service website @ www.getdrivernow.com" , "none")

  }
}



