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
import actors.CRMActor._
import javax.inject._
import com.google.inject.name.Named


object InBoundActor{
}

//Recieves messages from twilio via inbox and sends it to the websocket actor outs.
class InBoundActor @Inject() ( @Named("twilio-actor") twilioActor: ActorRef,
								@Named("crm-actor") crmActor: ActorRef,
					 			@Named("account-actor") accActor: ActorRef ) extends Actor {

	import InBoundActor._

  def receive = LoggingReceive {
						
	case StartUser(from, date, msg, fullname, email, phone, address,  hasstripe, preferences) =>	
										accActor ! Notifier(from, "NOTIFICATION", date, "initiated start request", fullname, "driverphone", true)
										//twilioActor ! SendSMS(from, "will get to you in a moment", "driverphone")
										crmActor ! SendSMSMsg(from, "We will get to you in a moment")

	case StartNone(from, date, msg, fullname, email, phone, address,  hasstripe, preferences) =>
		//twilioActor ! SendSMS(from, "please register for our service at home.getgatsby.com/", "driverphone")
		crmActor ! SendSMSMsg(from, "Please register for our service at home.getgatsby.com/")

	case StartError(from, date, msg, fullname, email, phone, address,  hasstripe, preferences) =>
	 //accActor ! Notifier(from, "NOTIFICATION", date, "initiated start request", "NoName", "driverphone", true)
	 //twilioActor ! SendSMS(from, "please register for our service at home.getgatsby.com/", "driverphone")
	 crmActor ! SendSMSMsg(from, "Please register for our service at home.getgatsby.com/")

	case GenUser(from, date, msg, fullname, email, phone, address,  hasstripe, preferences) => 
				if (hasstripe.startsWith("cus_")) {
					accActor ! Accounter(from, "INCOMMING", date, msg, fullname, "driverphone", false)
					//twilioActor ! SendSMS("+16197237161", "GetGatsby service requested", "driverphone")
					//crmActor ! SendSMSMsg("6197237161", "GetGatsby service requested")
				} else {
					//twilioActor ! SendSMS(from, "please complete your registration by updating your payment details at home.getgatsby.com/", "driverphone")
					crmActor ! SendSMSMsg(from, "Please complete your registration by entering your payment details at home.getgatsby.com/")
				}

	case GenNone(from, date, msg, fullname, email, phone, address,  hasstripe, preferences) =>
	 //twilioActor ! SendSMS(from, "please register for our service at home.getgatsby.com/", "driverphone")
	 crmActor ! SendSMSMsg(from, "Please register for our service at home.getgatsby.com/")

	case GenError(from, date, msg, fullname, email, phone, address,  hasstripe, preferences) => 
	 accActor ! Accounter(from, "INCOMMING", date, msg, "NoName", "driverphone", false)

	case OutOfOffice(from, date, msg, fullname, email, phone, address,  hasstripe, preferences) =>
	 //twilioActor ! SendSMS(from, "We provide services between the the hrs of 7pm - 8am , please see our service website at home.getgatsby.com/" , "driverphone")
	 crmActor ! SendSMSMsg(from, "We provide services between the the hrs of 8pm - 8am pacific time, please see our service website at www.getgatsby.com/")

  }
}



