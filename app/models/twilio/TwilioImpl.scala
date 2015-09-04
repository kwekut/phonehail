package models.twilio

import scala.collection.JavaConversions._
import com.twilio.sdk.TwilioRestClient
import com.twilio.sdk.TwilioRestException
import com.twilio.sdk.resource.factory.MessageFactory
import com.twilio.sdk.resource.instance.Message
import com.twilio.sdk.resource.list.MessageList
import java.util.ArrayList
import play.api.libs.concurrent.Execution.Implicits._
import java.util.List
import scala.concurrent.Future
import scala.util.{Success, Failure}
import org.apache.http.NameValuePair
import org.apache.http.message.BasicNameValuePair
import scala.util.Try
import java.util.UUID
import play.api.Logger
import com.twilio.sdk.client.TwilioCapability
import javax.inject._
import com.google.inject.name.Named
import services.user.AuthenticationEnvironment


class TwilioImpl @Inject() (val env: AuthenticationEnvironment) extends TwilioService {
	private val PHONE_FROM = "+441473379566"
	private val from = "+1 (907) 312-2505"
	private val sid = "AC3efddf571fafb24b11a8a885bd45c93b"
	private val token = "7fca4bdf2d06e0e86024a69c441c0b29"

	val client = new TwilioRestClient(sid, token)
	val messageFactory: MessageFactory = client.getAccount.getMessageFactory
	//val defaultPic = "http://res.cloudinary.com/demo/image/facebook/c_thumb,g_face,h_90,w_120/billclinton.jpg"
	val defaultPic ="@routes.Assets.versioned('images/Ggicon.png')"



	def sendSMS(to: String, msg: String, driverphone: String = "none") = Try {
		 Logger.info(s"Sending SMS to $to with text $msg")
	
	    val driverimage: Future[String] = 
	    	if (driverphone != "none") {
	    env.identityService.retrievebyphone(driverphone) flatMap {
	    	case Some(driver) => Future.successful { driver.image.getOrElse(defaultPic) }
	    	case None => Future.successful { defaultPic }
	    }
			} else {Future.successful (defaultPic) }

	driverimage.onComplete {
		case Success(driver) =>
		val params = new ArrayList[NameValuePair]
	    	params.add(new BasicNameValuePair("Body", msg));
	    	params.add(new BasicNameValuePair("To", to));
	    	params.add(new BasicNameValuePair("From", from));
	    	params.add(new BasicNameValuePair("MediaUrl", driver));

	    val message: Message = messageFactory.create(params)
	    	message.getSid

	    case Failure(ex) => Logger.info("This Twilio Error will never occur")
	    }
	} 


	def getSMSList(num: String) =  {

        val amt = java.lang.Integer.parseInt(num)
    	val messages: MessageList = client.getAccount.getMessages
    	messages
    } 

	def getSMS(msid: String) = Try {
    	val message: Message = client.getAccount.getMessage(msid)
    } 
}

//+1 (907) 312-2505
//ACCOUNT SID: AC3efddf571fafb24b11a8a885bd45c93b
//AUTH TOKEN: 7fca4bdf2d06e0e86024a69c441c0b29

// catch (TwilioRestException e) {
//     	Logger.info("Status is: " + e.getErrorMessage())
//     	throw TwilioRestException
//     }