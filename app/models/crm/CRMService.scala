package models.crm

import scala.util.Try
import play.api.libs.json._
import scala.concurrent.Future
import actors.CRMActor._

 /**
 * Give access to the Twilio object.
 */	
trait CRMService {

//Sending an SMS:
//https://restapi.crmtext.com/smapi/rest?method=sendsmsmsg&phone_number=&message= 
	def sendsmsmsg(phone: String, msg: String): Future[String]

//Sending an MMS:
//https://restapi.crmtext.com/smapi/rest?method=sendsmsmsg&phone_number=&message=&mmsurl=
	def sendmmsmsg(phone: String, msg: String, driverphone: String): Future[String]

//Sending an Campaign:
//https://restapi.crmtext.com/smapi/rest?method=sendcampaign&name=&message= 
	def sendcampaign(msg: String): Future[String]

//Opt-in Customer
//https://restapi.crmtext.com/smapi/rest?method=optincustomer&firstname=&lastname=&phone_number=
	def optincustomer(firstname: String, lastname: String, phone: String): Future[String]

//Opt-out Customer
//https://restapi.crmtext.com/smapi/rest?method=optoutcustomer&phone_number=
	def optoutcustomer(phone: String): Future[String]

	// def optinStatus(usr: User): String  

//Creating a Store and User:
//https://restapi.crmtext.com/smapi/rest?method=createstoreanduser&storename=&storeKeyword=&firstname&=lastname=&emailid= &phonenumber=&password= 
	def createstoreanduser(storename: String, storekeyword: String, firstname: String, lastname: String, 
															email: String, storenumber: String, password: String): Future[String]

//Set a Callback URL:
	def setcallback(url: String): Future[String] 

//Get Opt-In Status for a Mobile Number:
//https://restapi.crmtext.com/smapi/rest?method=getcustmsgsbymobile&phone_number=&startdate=&enddate=&startcount=&endcount= 
	def getcustmsgsbymobile(phone: String, startdate: String, enddate: String, startcount: String, 
																		messagecount: String): Future[Option[MsgList]]

//Get Inbound Messages By Date Range:
//https://restapi.crmtext.com/smapi/rest?method=getinboundmsgs&startdate=&enddate= 
	def getinboundmsgs(start: String, end: String): Future[Option[MsgList]]

//Get Outbound Messages By Date Range:
//https://restapi.crmtext.com/smapi/rest?method=getoutboundmsgs&startdate=&enddate= 
	def getoutboundmsgs(start: String, end: String): Future[Option[MsgList]]

//Get Opt-In Status for a Mobile Number:
//https://restapi.crmtext.com/smapi/rest?method=getcustomerinfo&phone_number= 
	def getcustomerinfo(fieldval: String, value: String): Future[Option[MsgList]]

}
