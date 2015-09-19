package actors

import akka.actor._
import javax.inject._
import com.google.inject.name.Named
import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.libs.concurrent.Execution.Implicits._
import services.user.AuthenticationEnvironment
import scala.concurrent.Future
import scala.util.{Success, Failure}
import actors.AccountActor._
//import shapeless.Typeable._
import models.crm.CRMService
import org.joda.time.LocalDateTime
import akka.event.LoggingReceive
import play.api.Configuration
import scala.collection.JavaConversions._



object CRMActor {

	case class SendSMSMsg(phone: String, msg: String)

	case class SendMMSMsg(phone: String, msg: String, driverphone: String)

	case class OptInCustomer(firstname: String, lastname: String, phone: String)

	case class OptOutCustomer(phone: String)

	case class CreateStoreandUser(storename: String, storeKeyword: String, firstname: String, lastname: String, emailid: String, phone: String, password: String)

	case class GetCustMsgsbyMobile(phone: String, startdate: String, enddate: String, startcount: String, endcount: String) 

	case class GetInboundMsgs(startdate: String, enddate: String)

	case class GetOutboundMsgs(startdate: String, enddate: String) 
 
	case class GetCustomerInfo(fieldval: String, value: String)

	case class SetCallBack(url: String)

	case class Msg(custID: String, message: String, keyword: String, mobileNum: String, optInStatus: String, timeStamp: String, 
					subacct: String, custName: String, msgID: String, subacct_name: String, mms: String)



  object Msg{
    implicit val msgReads: Reads[Msg] = (
      (JsPath \ "custID").read[String] and
      (JsPath \ "message").read[String] and
      (JsPath \ "keyword").read[String] and
      (JsPath \ "mobileNum").read[String] and
      (JsPath \ "optinStatus").read[String] and
      (JsPath \ "timeStamp").read[String] and
      (JsPath \ "subacct").read[String] and
      (JsPath \ "custName").read[String] and
      (JsPath \ "msgID").read[String] and
      (JsPath \ "subacct_name").read[String] and
      (JsPath \ "mms").read[String] 
    )(Msg.apply _)

    implicit val msgWrites: Writes[Msg] = (
      (JsPath \ "custID").write[String] and
      (JsPath \ "message").write[String] and
      (JsPath \ "keyword").write[String] and
      (JsPath \ "mobileNum").write[String] and
      (JsPath \ "optinStatus").write[String] and
      (JsPath \ "timeStamp").write[String] and
      (JsPath \ "subacct").write[String] and
      (JsPath \ "custName").write[String] and
      (JsPath \ "msgID").write[String] and
      (JsPath \ "subacct_name").write[String] and
      (JsPath \ "mms").write[String] 
    )(unlift(Msg.unapply))
  }



	case class Msgs(msglist: Seq[Msg])

	//implicit val msgsWrites: Writes[Msgs] =  (__ \ "msglist").write[Seq[Msg]].contramap { (msglist: Seq[Msg]) => msgs.msglist }
									
	implicit val msgsReads: Reads[Msgs] = (__ \ "msglist").read[Seq[Msg]].map { msglist => Msgs(msglist) }

}

class CRMActor @Inject() ( 
	val env: AuthenticationEnvironment, 
	crmSer: CRMService, 
	@Named("account-actor") accActor: ActorRef) extends Actor {
  
  import context.dispatcher
  import CRMActor._
  val icon = "http://www.ucarecdn.com/b275edaf-d627-499d-a827-a5a393bded0b/Ggicon.png"

  def receive = LoggingReceive {

//Sending an SMS:
//https://restapi.crmtext.com/smapi/rest?method=sendsmsmsg&phone_number=&result= 
	case SendSMSMsg(phone, message) => 
              val date = new LocalDateTime().toString() 
		crmSer.sendsmsmsg(phone, message)onComplete {		
        	case Success(msg) => accActor ! Notifier("crm", "NOTIFICATION", date, msg, "no-name", "driverphone", true) 
    		case Failure(ex) => accActor ! Notifier("crm", "NOTIFICATION", date, ex.getMessage(), "no-name", "driverphone", true)
    	}

//Sending an MMS:
//https://restapi.crmtext.com/smapi/rest?method=sendsmsmsg&phone_number=&result=&mmsurl=
	case SendMMSMsg(phone, message, driverphone) => 
	val date = new LocalDateTime().toString() 
	
	if (driverphone == "driverphone") {
			crmSer.sendsmsmsg(phone, message)onComplete {		
	        	case Success(msg) => accActor ! Notifier("crm", "NOTIFICATION", date, msg, "no-name", "driverphone", true) 
	    		case Failure(ex) => accActor ! Notifier("crm", "NOTIFICATION", date, ex.getMessage(), "no-name", "driverphone", true)
	    	}
	} else {
		env.identityService.retrievebyphone(driverphone) flatMap {
			case Some(driver) => 		
				val rt = crmSer.sendmmsmsg(phone, message, driver.image.getOrElse(icon)) 
				rt onComplete {		
		        	case Success(msg) => accActor ! Notifier("crm", "NOTIFICATION", date, msg, "no-name", driverphone, true)
		    		case Failure(ex) => accActor ! Notifier("crm", "NOTIFICATION", date, ex.getMessage(), "no-name", "driverphone", true)
		    	}
		    	rt
		    case None => 
				crmSer.sendsmsmsg(phone, message)onComplete {		
		        	case Success(msg) => accActor ! Notifier("crm", "NOTIFICATION", date, msg, "no-name", "driverphone", true) 
		    		case Failure(ex) => accActor ! Notifier("crm", "NOTIFICATION", date, ex.getMessage(), "no-name", "driverphone", true)
		    	}
		    		Future.successful { driverphone }
		}
	}
//Opt-in Customer
//https://restapi.crmtext.com/smapi/rest?method=optincustomer&firstname=&lastname=&phone_number=
	case OptInCustomer(firstname, lastname, phone) => 
		val date = new LocalDateTime().toString()
		crmSer.optincustomer(firstname, lastname, phone)onComplete {		
        	case Success(msg) => accActor ! Notifier("crm", "NOTIFICATION", date, msg, "no-name", "driverphone", true)
    		case Failure(ex) => accActor ! Notifier("crm", "NOTIFICATION", date, ex.getMessage(), "no-name", "driverphone", true)
    	}
//Opt-out Customer
//https://restapi.crmtext.com/smapi/rest?method=optoutcustomer&phone_number=
	case OptOutCustomer(phone) => 
		val date = new LocalDateTime().toString() 
		val msg = crmSer.optoutcustomer(phone) onComplete {		
        	case Success(msg) => accActor ! Notifier("crm", "NOTIFICATION", date, msg, "no-name", "driverphone", true)
    		case Failure(ex) => accActor ! Notifier("crm", "NOTIFICATION", date, ex.getMessage(), "no-name", "driverphone", true)
    	}
	// def optinStatus(usr: User): String  

	//Creating a Store and User:
	//https://restapi.crmtext.com/smapi/rest?method=createstoreanduser&storename=&storeKeyword=&firstname&=lastname=&emailid= &phonenumber=&password= 
	case CreateStoreandUser(storename, storeKeyword, firstname, lastname, emailid, phone, password) => 
    	val date = new LocalDateTime().toString()		
		crmSer.createstoreanduser(storename, storeKeyword, firstname, lastname, emailid, phone, password) onComplete {		
        	case Success(msg) => accActor ! Notifier("crm", "NOTIFICATION", date, msg, "no-name", "driverphone", true)
    		case Failure(ex) => accActor ! Notifier("crm", "NOTIFICATION", date, ex.getMessage(), "no-name", "driverphone", true)
    	}
		//Set a Callback URL:
		//https://restapi.crmtext.com/smapi/rest?method=setcallback&callback= 
	case SetCallBack(url) => 
		val date = new LocalDateTime().toString() 
		crmSer.setcallback(url) onComplete {		
        	case Success(msg) => accActor ! Notifier("crm", "NOTIFICATION", date, msg, "no-name", "driverphone", true)
    		case Failure(ex) =>	accActor ! Notifier("crm", "NOTIFICATION", date, ex.getMessage(), "no-name", "driverphone", true)
    	}

	//Get Opt-In Status for a Mobile Number:
	//https://restapi.crmtext.com/smapi/rest?method=getcustmsgsbymobile&phone_number=&startdate=&enddate=&startcount=&endcount= 
	case GetCustMsgsbyMobile(phone, startdate, enddate, startcount, endcount) =>  
		val date = new LocalDateTime().toString() 
		val results = crmSer.getcustmsgsbymobile(phone, startdate, enddate, startcount, endcount).map(xs => xs.map(_.msglist))
		results map {
			  case Some(results) =>  for ( result <- results) { accActor ! NoBuffer(result.mobileNum, "TWILIO", result.timeStamp, result.message, result.custName, "driverphone", true) }
			  case None => accActor ! Notifier("crm", "NOTIFICATION", date, "error", "no-name", "driverphone", true)
		}
	//Get Inbound Messages By Date Range:
	//https://restapi.crmtext.com/smapi/rest?method=getinboundmsgs&startdate=&enddate= 
	case GetInboundMsgs(startdate, enddate) => 
		val date = new LocalDateTime().toString() 
		val results = crmSer.getinboundmsgs(startdate, enddate).map(xs => xs.map(_.msglist))
		results map {
			  case Some(results) =>  for ( result <- results) { accActor ! NoBuffer(result.mobileNum, "TWILIO", result.timeStamp, result.message, result.custName, "driverphone", true) }
			  case None => accActor ! Notifier("crm", "NOTIFICATION", date, "error", "no-name", "driverphone", true)
		}
	//Get Outbound Messages By Date Range:
	//https://restapi.crmtext.com/smapi/rest?method=getoutboundmsgs&startdate=&enddate= 
	case GetOutboundMsgs(startdate, enddate) => 
		val date = new LocalDateTime().toString() 
		val results = crmSer.getoutboundmsgs(startdate, enddate).map(xs => xs.map(_.msglist))
		results map {
			  case Some(results) =>  for ( result <- results) { accActor ! NoBuffer(result.mobileNum, "TWILIO", result.timeStamp, result.message, result.custName, "driverphone", true) }
			  case None => accActor ! Notifier("crm", "NOTIFICATION", date, "error", "no-name", "driverphone", true)
		}												
	//Get Opt-In Status for a Mobile Number:
	//https://restapi.crmtext.com/smapi/rest?method=getcustomerinfo&phone_number= 
	case GetCustomerInfo(fieldval, value) => 
		val date = new LocalDateTime().toString() 
		val results = crmSer.getcustomerinfo(fieldval, value).map(xs => xs.map(_.msglist))
		results map {
			  case Some(results) =>  for ( result <- results) { accActor ! NoBuffer(result.mobileNum, "TWILIO", result.timeStamp, result.message, result.custName, "driverphone", true) }
			  case None => accActor ! Notifier("crm", "NOTIFICATION", date, "error", "no-name", "driverphone", true)
		}			
		
  }
}
