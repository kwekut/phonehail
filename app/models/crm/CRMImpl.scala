package models.crm

import javax.inject.Inject
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import org.apache.commons.codec.binary.Base64._
import java.util.Base64
import actors.CRMActor._
import play.api.Logger
import play.api.mvc._
import play.api.libs.ws._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import javax.inject._
import com.google.inject.name.Named
import services.user.AuthenticationEnvironment
//new String(decodeBase64(encoded.getBytes))
//encodeBASE64(java.lang.String value) 

class CRMImpl @Inject() (val ws: WSClient, val env: AuthenticationEnvironment) extends CRMService {

	private val user = "kwekut@gmail.com"
	private val password = "4myselfiam:CRM39241"
	private val keyword = "CRM39241"
	//private val auth = Base64.encodeBASE64("kwekut@gmail.com:password:CRM39241")
	private val url = "https://restapi.crmtext.com/smapi/rest?"

	private val request: WSRequest = ws.url(url)
	private val authRequest: WSRequest = ws.url(url).withAuth(user, password, WSAuthScheme.BASIC) 
	private val authTimeRequest: WSRequest =  ws.url(url)
												  	.withAuth(user, password, WSAuthScheme.BASIC)
												    .withRequestTimeout(10000)

//Sending an SMS:
//https://restapi.crmtext.com/smapi/rest?method=sendsmsmsg&phone_number=&message= 
	def sendsmsmsg(phone: String, msg: String) = {

		val complexResponse =
		  	request.withAuth(user, password, WSAuthScheme.BASIC)
		    .withRequestTimeout(10000)
		    .post(Map("method" -> Seq("sendsmsmsg"), "phone_number" -> Seq(phone), "message" -> Seq(msg)))

		val futureStatus = complexResponse.map {
		  response =>
		    response.status
		}

		val futureResult = complexResponse.map {
		  response =>
		    response.body
		}
		val result = for {
			//st <- futureStatus
		    elem <- futureResult //if st != 200
		  } yield elem.slice(21, 121)

		result
//only on failure
	}

//Sending an MMS:
//https://restapi.crmtext.com/smapi/rest?method=sendsmsmsg&phone_number=&message=&mmsurl=
//Sending an MMS:
//https://restapi.crmtext.com/smapi/rest?method=sendsmsmsg&phone_number=&message=&mmsurl=
	def sendmmsmsg(phone: String, msg: String, mms: String) = {

		val complexRequest =
		  	request.withAuth(user, password, WSAuthScheme.BASIC)
		    .withRequestTimeout(10000)
		    .post(Map("method" -> Seq("sendsmsmsg"), "phone_number" -> Seq(phone), "message" -> Seq(msg), "mmsurl" -> Seq(mms)))

		val futureResult: Future[scala.xml.NodeSeq] = complexRequest.map {
		  response =>
		    response.xml \ "message"
		}
		val result = for {
		    elem <- futureResult 
		  } yield elem.text.mkString
		result

	}

//Opt-in Customer
//https://restapi.crmtext.com/smapi/rest?method=optincustomer&firstname=&lastname=&phone_number=
	def optincustomer(firstname: String, lastname: String, phone: String) = {

		val complexResponse =
		  	request.withAuth(user, password, WSAuthScheme.BASIC)
		    .withRequestTimeout(10000)
		    .post(Map("method" -> Seq("optincustomer"), "firstname" -> Seq(firstname), "lastname" -> Seq(lastname), "phone_number" -> Seq(phone) ))

		val futureResult = complexResponse.map {
		  response =>
		    response.body
		}
		val result = for {
		    elem <- futureResult 
		  } yield elem.slice(21, 121)
		result
	}

//Opt-out Customer
//https://restapi.crmtext.com/smapi/rest?method=optoutcustomer&phone_number=
	def optoutcustomer(phone: String) = {
		
		val complexResponse =
	  	request.withAuth(user, password, WSAuthScheme.BASIC)
	    .withRequestTimeout(10000)
	    .post(Map("method" -> Seq("optoutcustomer"), "phone_number" -> Seq(phone)))

		val futureResult = complexResponse.map {
		  response =>
		    response.body
		}
		val result = for {
		    elem <- futureResult 
		  } yield elem.slice(21, 121)
		result
	}

	// def optinStatus(usr: User): String = {
	// 	val complexRequest: WSRequest =
	// 	  request.withAuth(user, password, WSAuthScheme.BASIC)
	// 	    .withRequestTimeout(10000)
	// 	    .withQueryString("search" -> "play")//firstname, lastname, phone_number

	// 	val futureResult: Future[scala.xml.NodeSeq] = complexRequest.get().map {
	// 	  response =>
	// 	    response.xml \ "message"
	// 	}
	// 	futureResult.toString
	// } 

//Creating a Store and User:
//https://restapi.crmtext.com/smapi/rest?method=createstoreanduser&storename=&storeKeyword=&firstname&=lastname=&emailid= &phonenumber=&password= 
	def createstoreanduser(storename: String, storeKeyword: String, firstname: String, lastname: String, 
															emailid: String, phone: String, password: String) =  {
		 //Logger.info(s"Sending SMS to $to with text $msg")

		val complexResponse =
		  	request.withAuth(user, password, WSAuthScheme.BASIC)
		    .withRequestTimeout(10000)
		    .post(Map("method" -> Seq("createstoreanduser"), "storename" -> Seq(storename), "storeKeyword" -> Seq(keyword),  "firstname" -> Seq(firstname), "lastname" -> Seq(lastname), "email" -> Seq(lastname), "phone_number" -> Seq(phone), "password" -> Seq(password)))

		val futureResult = complexResponse.map {
		  response =>
		    response.body
		}
		val result = for {
		    elem <- futureResult 
		  } yield elem.slice(21, 121)
		result
	}

//Set a Callback URL:
//https://restapi.crmtext.com/smapi/rest?method=setcallback&callback= 
	def setcallback(url: String) =  {
		 //Logger.info(s"Sending SMS to $to with text $msg")
	
		val complexResponse =
		  	request.withAuth(user, password, WSAuthScheme.BASIC)
		    .withRequestTimeout(10000)
		    .post(Map("method" -> Seq("setcallback"), "callback" -> Seq(url)))

		val futureResult = complexResponse.map {
		  response =>
		    response.body
		}
		val result = for {
		    elem <- futureResult 
		  } yield elem.slice(21, 121)
		result
	}


//Get Opt-In Status for a Mobile Number:
//https://restapi.crmtext.com/smapi/rest?method=getcustmsgsbymobile&phone_number=&startdate=&enddate=&startcount=&endcount= 
	def getcustmsgsbymobile(phone: String, startdate: String, enddate: String, startcount: String, messagecount: String) = {
		val complexRequest =
		  request.withAuth(user, password, WSAuthScheme.BASIC)
		    .withRequestTimeout(10000)
		    .withQueryString("method" -> "getcustmsgsbymobile", "phone_number" -> phone, "startdate" -> startdate, "enddate" -> enddate, "startcount" -> startcount, "msgcount" -> messagecount)

		val futureResponse = complexRequest.get().map {
		  response =>
		    (response.json).asOpt[MsgList] 
		}

		futureResponse
	} 

//Get Inbound Messages By Date Range:
//https://restapi.crmtext.com/smapi/rest?method=getinboundmsgs&startdate=&enddate= 
	def getinboundmsgs(startdate: String, enddate: String)= {
		val complexRequest =
		  request.withAuth(user, password, WSAuthScheme.BASIC)
		    .withRequestTimeout(10000)
		    .withQueryString("method" -> "getinboundmsgs", "startdate" -> startdate, "enddate" -> enddate)

		val futureResponse = complexRequest.get().map {
		  response =>
		    (response.json).asOpt[MsgList] 
		}
		futureResponse
	} 

//Get Outbound Messages By Date Range:
//https://restapi.crmtext.com/smapi/rest?method=getoutboundmsgs&startdate=&enddate= 
	def getoutboundmsgs(startdate: String, enddate: String) = {
		val complexRequest =
		  request.withAuth(user, password, WSAuthScheme.BASIC)
		    .withRequestTimeout(10000)
		    .withQueryString("method" -> "getoutboundmsgs", "startdate" -> startdate, "enddate" -> enddate)

		val futureResult = complexRequest.get().map {
		  response =>
		    (response.json).asOpt[MsgList]
		}
		futureResult 
	} 

//Get Opt-In Status for a Mobile Number:
//https://restapi.crmtext.com/smapi/rest?method=getcustomerinfo&phone_number= 
	def getcustomerinfo(fieldval: String, value: String) =  {
		 //Logger.info(s"Sending SMS to $to with text $msg")
		val complexRequest =
		  	request.withAuth(user, password, WSAuthScheme.BASIC)
		    .withRequestTimeout(10000)
		    .withQueryString("method" -> "getcustomerinfo", fieldval -> value)

		val futureResult = complexRequest.get().map {
		  response =>
		    (response.json).asOpt[MsgList]
		}
		futureResult
	}

}