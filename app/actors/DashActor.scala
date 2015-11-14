package actors

import akka.actor._
import play.api.Play.current
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Success, Failure}
import models.user.{ User, Dash, UserForms, AdminUserUpdateData, AdminSearchData, AdminDashUpdateData, Role }
import play.api.Logger
import java.util.UUID
import akka.event.LoggingReceive
import services.user.AuthenticationEnvironment
import org.joda.time.LocalDateTime
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import org.joda.time.DateTimeZone
import org.joda.time.DateTime
import javax.inject._
import com.google.inject.name.Named
import actors.StripeActor._

object DashActor {
	val LA: DateTimeZone = DateTimeZone.forID("America/Los_Angeles")
	val dtf: DateTimeFormatter = DateTimeFormat.forPattern("yyyy-mm-dd HH:MM:SS Z")
	val dta: DateTimeFormatter = DateTimeFormat.forPattern("yyyy-mm-dd HH:MM:SS")
	//class StorageException(msg: String) extends RuntimeException(msg)
}

class DashActor @Inject() (val env: AuthenticationEnvironment) extends Actor {

 	import DashActor._

  def receive = LoggingReceive {

		case UpdateDash(phone, striperesponse, pickuplocation, amt, name, driverphone, date) => 
              			Logger.info("DashActor: Client and Driver available-Top")
      	env.identityService.retrievebyphone(phone) onComplete {		
        	case Success(user) => 
        		env.identityService.retrievebyphone(driverphone).flatMap {
              		case Some(driver) => 
              			val local = dtf.withZone(LA).parseDateTime(DateTime.now.toString("yyyy-mm-dd HH:MM:SS Z")).toString("yyyy-mm-dd HH:MM:SS")
              			val created = LocalDateTime.parse(local, dta)
              			val dash = new Dash(UUID.randomUUID(), None, None, None, None, None, None, None, None, None, None, None, None, None, None, created)
              		    val newDash = dash.copy(
						      clientphone = Some(phone),
						      clientname =  Some(user.get.fullName.getOrElse("")),
						      clientzip =  Some(user.get.zip.getOrElse("")),
						      clientcity = Some(user.get.city.getOrElse("")),
						      clientstate = Some(user.get.state.getOrElse("")),
						      clientemail = Some(user.get.email.getOrElse("")),
						      driverphone = Some(driverphone),
						      drivername = Some(driver.fullName.getOrElse("")),
						      drivercompany = Some(driver.preferences.getOrElse("")),
						      pickuplocation = Some(pickuplocation),
						      attendantnamecomment = Some(name),
						      chargedamount = Some(amt),
						      chargecomment = Some(striperesponse)
						      //other =  Some(data.other)
    					)
    					Logger.info("DashActor: Client and Driver available")
              			env.dashService.save(newDash) 
              			
              		case None =>
              			val local = dtf.withZone(LA).parseDateTime(DateTime.now.toString("yyyy-mm-dd HH:MM:SS Z")).toString("yyyy-mm-dd HH:MM:SS") 
              			val created = LocalDateTime.parse(local, dta)
              			val dash = new Dash(UUID.randomUUID(), None, None, None, None, None, None, None, None, None, None, None, None, None, None, created)
              		    val newDash = dash.copy(
						      clientphone = Some(phone),
						      clientname =  Some(user.get.fullName.getOrElse("")),
						      clientzip =  Some(user.get.zip.getOrElse("")),
						      clientcity = Some(user.get.city.getOrElse("")),
						      clientstate = Some(user.get.state.getOrElse("")),
						      clientemail = Some(user.get.email.getOrElse("")),
						      driverphone = Some(driverphone),
						      drivername = Some("NA"),
						      drivercompany = Some("NA"),
						      pickuplocation = Some(pickuplocation),
						      attendantnamecomment = Some(name),
						      chargedamount = Some(amt),
						      chargecomment = Some(striperesponse)
						      //other =  Some(data.other)
						)
						Logger.info("DashActor: Client Available and No Driver available")
              			env.dashService.save(newDash)
              			
            	}

			case Failure(ex) => 
						val local = dtf.withZone(LA).parseDateTime(DateTime.now.toString("yyyy-mm-dd HH:MM:SS Z")).toString("yyyy-mm-dd HH:MM:SS")
						val created = LocalDateTime.parse(local, dta)
						val dash = new Dash(UUID.randomUUID(), None, None, None, None, None, None, None, None, None, None, None, None, None, None, created)
					    val newDash = dash.copy(
					    	  clientphone = Some(phone),
					    	  driverphone = Some(driverphone),
						      pickuplocation = Some(pickuplocation),
						      attendantnamecomment = Some(name),
						      chargedamount = Some(amt),
						      chargecomment = Some(striperesponse)
						      //other =  Some(data.other)
						)
						env.dashService.save(newDash)
						Logger.info("DashActor: No Client, No Driver available")
						Logger.info(s"${ex.getMessage()}")
		}
		


		// case Message(mid, from, date, msg) => 
  //     	env.identityService.retrievebyphone(from) onComplete {		
  //       	case Success(user) =>  Logger.info(s"user retrieve by phone ${user}")
  //       	if(user.isDefined) {

  //       			inboundActor ! GenUser(from, date, msg, user.get.fullName.getOrElse(""), user.get.email.getOrElse(""), user.get.phone.getOrElse(""), 
  //       				user.get.street.getOrElse("") + ":" + user.get.city.getOrElse("") + ":" + user.get.state.getOrElse("") + ":" + user.get.zip.getOrElse(""),  user.get.hasstripe.getOrElse(""), user.get.preferences.getOrElse(""))
  //       		} else {
  //       	 		inboundActor ! GenNone(from, date, msg, "no-fullname", "no-email", "no-phone", "no-address",  "Empty", "no-preferences")
  //       		}
		// 	case Failure(ex) => inboundActor ! GenError(from, date, msg, "no-fullname", "no-email", "no-phone", "no-address",  "Empty", "no-preferences")
		// }

 	}
}

