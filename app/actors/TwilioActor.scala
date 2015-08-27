package actors

import akka.actor._
import javax.inject._
import com.google.inject.name.Named
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import play.api.libs.concurrent.Execution.Implicits._
import actors.AccountActor._
import akka.event.LoggingReceive
import play.api.Configuration
import models.twilio.TwilioService
import com.twilio.sdk.resource.instance.Message
import com.twilio.sdk.resource.list.MessageList
import scala.collection.JavaConversions._



object TwilioActor {
	case class SendSMS(to: String, body: String, driver: String)
	case class GetSMSList(num: String)
}

class TwilioActor @Inject() ( @Named("account-actor") accActor: ActorRef, twilioSer: TwilioService) extends Actor {
  import TwilioActor._

  def receive = LoggingReceive {

	case SendSMS(to, body, driver) => twilioSer.sendSMS(to, body, driver) 

	case GetSMSList(num) => 
		val messages: MessageList = twilioSer.getSMSList(num)
    	for ( message <- messages) accActor ! NoBuffer(message.getFrom, "TWILIO", message.getDateSent.toString, message.getBody, "no-name", "driverphone", true)
			
  }
}

//+1 (907) 312-2505

    // MessageList messages = client.getAccount().getMessages();
     
    // // Loop over messages and print out a property for each one.
    // for (Message message : messages) {
    //   System.out.println(message.getBody());
    // }
